package zeus.zeusverifier.node.modelchecking;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import zeus.shared.formula.Formula;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.*;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.compiler.utils.ParentStatement;
import zeus.shared.message.payload.abstraction.AbstractLiteral;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeModuleModelChecker {
  UUID verificationUuid;
  ClientCodeModule codeModule;
  Map<String, VariableInformation> variables;
  ModelCheckingNode modelCheckingNode;

  Queue<ParentStatement> currentStatementParents;
  List<Component> currentComponents;
  int currentIndex;

  Map<UUID, Predicate> predicates;
  Map<UUID, PredicateValuation> predicateValuations;
  Path path;

  public CodeModuleModelChecker(
    UUID verificationUuid,
    ClientCodeModule codeModule,
    ModelCheckingNode modelCheckingNode
  ) {
    this.verificationUuid = verificationUuid;
    this.codeModule = codeModule;
    this.modelCheckingNode = modelCheckingNode;
    Optional<Map<String, VariableInformation>> variablesOptional = codeModule.getVariables();

    if (variablesOptional.isEmpty()) {
      this.modelCheckingNode.sendMessage(new Message<>(new ModelCheckingFailed(
        this.modelCheckingNode.getUuid(),
        "code module variable information not present"
      ), new Recipient(NodeType.ROOT)));
      return;
    }

    this.variables = variablesOptional.get();

    this.currentStatementParents = null;
    this.currentComponents = new ArrayList<>();
    this.currentIndex = 0;
    this.predicates = new HashMap<>();
    this.predicateValuations = new HashMap<>();
    this.path = new Path(new ArrayList<>());
  }

  private void updateCurrentComponents() {
    this.currentComponents = this.currentStatementParents.isEmpty()
      ? this.codeModule.getComponents()
      : ((LinkedList<ParentStatement>) this.currentStatementParents).peekLast().getComponents();
  }

  public boolean calibrate(StartModelCheckingTaskRequest startModelCheckingTaskRequest) {
    Path path = startModelCheckingTaskRequest.getPath();
    Optional<ComponentSearchResult> componentSearchResultOptional = path.getStates().isEmpty()
      ? this.codeModule.getFirstComponent()
      : this.codeModule.searchComponent(path.getStates().getLast().getLocation());

    if (componentSearchResultOptional.isEmpty()) {
      return false;
    }

    ComponentSearchResult componentSearchResult = componentSearchResultOptional.get();
    this.currentIndex = componentSearchResult.getIndex();
    this.currentStatementParents = componentSearchResult.getParents();
    this.updateCurrentComponents();
    this.path = path;
    this.predicates = path.getPredicates().stream()
      .collect(Collectors.toMap(Predicate::getUuid, predicate -> predicate));

    startModelCheckingTaskRequest.getPredicateValuations().ifPresent(uuidPredicateValuationMap ->
      this.predicateValuations = uuidPredicateValuationMap);

    return true;
  }

  private Optional<Component> getNextComponent(
    int currentIndex,
    List<Component> currentComponents,
    Queue<ParentStatement> currentStatementParents
  ) {
    if (currentIndex >= currentComponents.size() - 1) {
      if (currentStatementParents.isEmpty()) {
        return Optional.empty();
      }

      ParentStatement parentStatement = ((LinkedList<ParentStatement>) currentStatementParents).peekLast();

      if (parentStatement.getControlStatement() instanceof WhileStatement) {
        return Optional.of(parentStatement.getControlStatement());
      }

      LinkedList<ParentStatement> nextParentStatements = new LinkedList<>(
        ((LinkedList<ParentStatement>) currentStatementParents).subList(
          0,
          currentStatementParents.size() - 1
        )
      );

      return getNextComponent(
        parentStatement.getIndex(),
        (nextParentStatements.isEmpty())
          ? this.codeModule.getComponents()
          : nextParentStatements.peekLast().getComponents(),
        nextParentStatements
      );
    }

    return Optional.of(currentComponents.get(currentIndex + 1));
  }

  private Optional<Component> getNextComponent() {
    return this.getNextComponent(this.currentIndex, this.currentComponents, this.currentStatementParents);
  }

  private Optional<AbstractLiteral> evaluateExpression(Expression expression) {
    try {
      return Optional.of(this.modelCheckingNode.sendAbstractRequest(
        this.verificationUuid,
        this.predicates,
        this.predicateValuations,
        expression.toFormula(this.variables),
        new Location(expression.getLine(), expression.getLinePosition())
      ).get());
    } catch (InterruptedException | ExecutionException e) {
      this.modelCheckingNode.sendMessage(new Message<>(
        new ModelCheckingFailed(
          this.modelCheckingNode.getUuid(),
          String.format("could not evaluate expression at %s:%s", expression.getLine(), expression.getLinePosition())
        ), new Recipient(NodeType.ROOT)));
      return Optional.empty();
    }
  }

  private Optional<ModelCheckingResult> handleControlStatement(ControlStatement controlStatement, AbstractLiteral abstractLiteral) {
    Optional<ModelCheckingResult> modelCheckingResultOptional = Optional.empty();

    if (controlStatement instanceof WhileStatement) {
      Optional<Boolean> componentVisitedOptional = this.modelCheckingNode.checkIfComponentVisited(
        this.verificationUuid,
        new Location(controlStatement.getLine(), controlStatement.getLinePosition()),
        new HashSet<>(this.predicateValuations.values())
      );

      if (componentVisitedOptional.isEmpty()) {
        return Optional.of(new ModelCheckingResult(ModelCheckingResultStatus.CHECK_IF_COMPONENT_VISITED_FAILED));
      }

      if (componentVisitedOptional.get()) {
        modelCheckingResultOptional = Optional.of(new ModelCheckingResult(
          ModelCheckingResultStatus.COMPONENT_ALREADY_VISITED
        ));
      }

      this.modelCheckingNode.addVisitedComponent(
        this.verificationUuid,
        new Location(controlStatement.getLine(), controlStatement.getLinePosition()),
        new HashSet<>(this.predicateValuations.values())
      );
    }


    switch (abstractLiteral) {
      case TRUE -> {
        if (modelCheckingResultOptional.isPresent()) {
          return modelCheckingResultOptional;
        }

        this.currentStatementParents.add(new ParentStatement(
        controlStatement,
        (controlStatement instanceof IfStatement)
          ? ((IfStatement) controlStatement).getThenBody().getBodyComponents()
          : ((WhileStatement) controlStatement).getBody().getBodyComponents(),
        this.currentIndex
      ));
        this.currentIndex = 0;
        this.updateCurrentComponents();
      }

      case FALSE -> {
        if (controlStatement instanceof WhileStatement || ((IfStatement) controlStatement).getElseBody() == null) {
          this.currentIndex++;
          return Optional.empty();
        }

        this.currentStatementParents.add(new ParentStatement(
          controlStatement,
          ((IfStatement) controlStatement).getElseBody().getBodyComponents(),
          this.currentIndex
        ));

        this.currentIndex = 0;
        this.updateCurrentComponents();
      }
      case NON_DETERMINISTIC -> {
        List<BodyComponent> bodyComponents = (controlStatement instanceof WhileStatement)
          ? ((WhileStatement) controlStatement).getBody().getBodyComponents()
          : ((IfStatement) controlStatement).getThenBody().getBodyComponents();

        BodyComponent bodyComponent = bodyComponents.getFirst();

        if (modelCheckingResultOptional.isEmpty()) {
          this.modelCheckingNode.sendMessage(new Message<>(new DistributeModelCheckingRequest(
            UUID.randomUUID(),
            this.verificationUuid,
            new Path(Stream.concat(
              this.path.getStates().stream(),
              Stream.of(new State(new Location(bodyComponent.getLine(), bodyComponent.getLinePosition()), false))
            ).toList()),
            new ArrayList<>(List.of(this.predicateValuations))
          ), new Recipient(NodeType.MODEL_CHECKING_GATEWAY)));
        }

        return this.handleControlStatement(controlStatement, AbstractLiteral.FALSE);
      }
    }
    return Optional.empty();
  }

  private void handleCurrentParentStatement() {
    ParentStatement parentStatement = ((LinkedList<ParentStatement>) this.currentStatementParents).pollLast();
    this.updateCurrentComponents();

    if (parentStatement.getControlStatement() instanceof WhileStatement) {
      Optional<AbstractLiteral> abstractionLiteralOptional = this.evaluateExpression(
        parentStatement.getControlStatement().getConditionExpression()
      );

      if (abstractionLiteralOptional.isEmpty()) {
        return;
      }

      AbstractLiteral abstractLiteral = abstractionLiteralOptional.get();

      if (abstractLiteral != AbstractLiteral.FALSE) {
        ControlStatement controlStatement = parentStatement.getControlStatement();
        this.path.getStates().add(new State(new Location(controlStatement.getLine(), controlStatement.getLinePosition())));
      }

      this.handleControlStatement(parentStatement.getControlStatement(), abstractLiteral);
      return;
    }

    this.currentIndex = parentStatement.getIndex() + 1;
  }

  private void handleAssignment(String variable, Expression expression, Context context, Solver solver) {
    Optional<Component> nextComponentOptional = this.getNextComponent();

    if (nextComponentOptional.isEmpty()) {
      return;
    }

    Formula expressionFormula = expression.toFormula(this.variables);

    Set<Predicate> relevantPredicates = this.predicates.values().stream()
      .filter(predicate -> predicate.getFormula().getReferencedVariables().contains(variable))
      .collect(Collectors.toSet());

    Set<Predicate> relevantPredicatesWeakestPrecondition = relevantPredicates.stream()
      .map(predicate -> new Predicate(
        predicate.getUuid(),
        predicate.getFormula().replace(variable, expressionFormula)
      ))
      .collect(Collectors.toSet());

    if (relevantPredicatesWeakestPrecondition.isEmpty()) {
      return;
    }

    Map<UUID, CompletableFuture<AbstractLiteral>> predicateCompletableFutures = relevantPredicatesWeakestPrecondition.stream()
      .map(predicate -> Map.entry(
        predicate.getUuid(), this.modelCheckingNode.sendAbstractRequest(
          this.verificationUuid,
          this.predicates,
          this.predicateValuations,
          predicate.getFormula(),
          new Location(expression.getLine(), expression.getLinePosition()))))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    HashMap<UUID, PredicateValuation> deterministicPredicateValuations = new HashMap<>();
    List<UUID> nonDeterministicPredicateValuations = new ArrayList<>();
    for (Map.Entry<UUID, CompletableFuture<AbstractLiteral>> predicateCompletableFuture : predicateCompletableFutures.entrySet()) {
      try {
        AbstractLiteral abstractLiteral = predicateCompletableFuture.getValue().get();
        PredicateValuation predicateValuation = this.predicateValuations.get(predicateCompletableFuture.getKey());
        switch (abstractLiteral) {
          case TRUE -> {
            predicateValuation.setValue(true);
            deterministicPredicateValuations.put(
              predicateCompletableFuture.getKey(),
              new PredicateValuation(predicateCompletableFuture.getKey(), true)
            );
          }
          case FALSE -> {
            predicateValuation.setValue(false);
            deterministicPredicateValuations.put(
              predicateCompletableFuture.getKey(),
              new PredicateValuation(predicateCompletableFuture.getKey(), false)
            );
          }
          case NON_DETERMINISTIC -> nonDeterministicPredicateValuations.add(predicateCompletableFuture.getKey());
        }
      } catch (InterruptedException | ExecutionException e) {
        this.modelCheckingNode.sendMessage(new Message<>(
          new ModelCheckingFailed(this.modelCheckingNode.getUuid(), "abstraction request failed"),
          new Recipient(NodeType.ROOT)
        ));
        break;
      }
    }

    if (deterministicPredicateValuations.size() == relevantPredicates.size()) {
      return;
    }

    List<Map<UUID, PredicateValuation>> predicateValuations = PredicateValuation.getCombinations(
      Stream.concat(
        deterministicPredicateValuations.entrySet().stream(),
        this.predicateValuations.entrySet().stream()
          .filter(predicatesValuationsEntry -> relevantPredicates.stream()
            .noneMatch(predicate -> predicate.getUuid().equals(predicatesValuationsEntry.getKey())))
      ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
      nonDeterministicPredicateValuations
    ).stream()
      .filter(valuations -> this.predicateValuationsInfeasible(
        valuations,
        context,
        solver
      ))
      .toList();

    if (predicateValuations.isEmpty()) {
      this.modelCheckingNode.sendMessage(new Message<>(
        new ModelCheckingFailed(
          this.modelCheckingNode.getUuid(),
          "assignment resulted in empty predicate valuations"
        ),
        new Recipient(NodeType.ROOT)
      ));
      return;
    }

    this.predicateValuations = predicateValuations.getFirst();
    Component nextComponent = nextComponentOptional.get();

    this.modelCheckingNode.sendMessage(new Message<>(
      new DistributeModelCheckingRequest(
        UUID.randomUUID(),
        this.verificationUuid,
        new Path(Stream.concat(
          this.path.getStates().stream(),
          Stream.of(new State(
            new Location(nextComponent.getLine(), nextComponent.getLinePosition()),
            new HashSet<>(this.predicates.values()),
            false
          ))
      ).toList()), predicateValuations.subList(1, predicateValuations.size())),
      new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
    ));
  }

  private boolean predicateValuationsInfeasible(
    Map<UUID, PredicateValuation> predicateValuations,
    Context context,
    Solver solver
  ) {
    List<Expr> formulas = new ArrayList<>();
    for (Map.Entry<UUID, Predicate> predicateEntry : this.predicates.entrySet()) {
      PredicateValuation predicateValuation = predicateValuations.get(predicateEntry.getKey());

      if (predicateValuation == null) {
        this.modelCheckingNode.sendMessage(new Message<>(
          new ModelCheckingFailed(
            this.modelCheckingNode.getUuid(),
            String.format("missing valuation for predicate \"%s\"", predicateEntry.getKey())
          ),
          new Recipient(NodeType.ROOT)
        ));
        return false;
      }

      formulas.add((predicateValuation.getValue())
        ? predicateEntry.getValue().getFormula().toFormula(context)
        : context.mkNot(predicateEntry.getValue().getFormula().toFormula(context)));
    }

    return solver.check(context.mkAnd(formulas.toArray(Expr[]::new))) == Status.SATISFIABLE;
  }

  public ModelCheckingResult checkComponents(Context context, Solver solver) {
    if (!this.predicateValuationsInfeasible(this.predicateValuations, context, solver)) {
      System.out.println("Infeasible predicate valuations, stopping model checking task");
      return new ModelCheckingResult(ModelCheckingResultStatus.INFEASIBLE_PREDICATE_VALUATIONS);
    }

    while (true) {
      if (this.currentIndex >= this.currentComponents.size()) {
        if (this.currentStatementParents.isEmpty()) {
          return new ModelCheckingResult(ModelCheckingResultStatus.NO_COUNTEREXAMPLE_FOUND);
        }

        this.handleCurrentParentStatement();

        if (this.currentIndex >= this.currentComponents.size()) {
          continue;
        }
      }

      Component component = this.currentComponents.get(this.currentIndex);

      if (this.path.getStates().isEmpty() || this.path.getStates().getLast().isChecked()) {
        this.path.getStates().add(new State(new Location(component.getLine(), component.getLinePosition())));
      }

      this.path.getStates().getLast().setChecked(true);

      switch (component) {
        case ControlStatement controlStatement -> {
          System.out.println("control statement");
          Optional<AbstractLiteral> abstractionLiteralOptional = this.evaluateExpression(
            controlStatement.getConditionExpression()
          );

          if (abstractionLiteralOptional.isEmpty()) {
            return new ModelCheckingResult(ModelCheckingResultStatus.ABSTRACTION_FAILED);
          }

          Optional<ModelCheckingResult> modelCheckingResultOptional = this.handleControlStatement(
            controlStatement,
            abstractionLiteralOptional.get()
          );

          if (modelCheckingResultOptional.isPresent()) {
            return modelCheckingResultOptional.get();
          }

          continue;
        }
        // case Input input -> {}
        case Output output -> {
          output.getDeclarationExpression().ifPresent(expression -> this.handleAssignment(
            output.getId(),
            expression,
            context,
            solver
          ));
        }
        case DeclarationVariableStatement declarationVariableStatement -> {
          System.out.println("declaration statement");
          declarationVariableStatement.getDeclarationExpression().ifPresent(expression -> this.handleAssignment(
            declarationVariableStatement.getId(),
            expression,
            context,
            solver
          ));
        }
        case AssignmentStatement assignmentStatement -> {
          System.out.println("assignment statement");
          this.handleAssignment(
            assignmentStatement.getId(),
            assignmentStatement.getAssignExpression(),
            context,
            solver
          );
        }
        case AssertStatement assertStatement -> {
          System.out.println("assert statement");
          Optional<AbstractLiteral> abstractLiteralOptional = this.evaluateExpression(assertStatement.getExpression());

          if (abstractLiteralOptional.isEmpty()) {
            return new ModelCheckingResult(ModelCheckingResultStatus.ABSTRACTION_FAILED);
          }

          if (abstractLiteralOptional.get() != AbstractLiteral.TRUE) {
            return new ModelCheckingResult(this.path);
          }
        }
        default -> new ModelCheckingResult(ModelCheckingResultStatus.UNSUPPORTED_COMPONENT);
      }

      this.currentIndex++;
    }
  }

  public ModelCheckingResult check() {
    try (Context context = new Context()) {
      return this.checkComponents(context, context.mkSolver());
    }
  }
}
