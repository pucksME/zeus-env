package zeus.zeusverifier.node.modelchecking;

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
  ClientCodeModule codeModule;
  Map<String, VariableInformation> variables;
  ModelCheckingNode modelCheckingNode;

  Queue<ParentStatement> currentStatementParents;
  List<Component> currentComponents;
  int currentIndex;

  Map<UUID, Predicate> predicates;
  Map<UUID, PredicateValuation> predicateValuations;
  Path path;

  public CodeModuleModelChecker(ClientCodeModule codeModule, ModelCheckingNode modelCheckingNode) {
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

  public boolean calibrate(Path path) {
    Optional<ComponentSearchResult> componentSearchResultOptional = path.states().isEmpty()
      ? this.codeModule.getFirstComponent()
      : this.codeModule.searchComponent(path.states().getLast().getLocation());

    if (componentSearchResultOptional.isEmpty()) {
      return false;
    }

    ComponentSearchResult componentSearchResult = componentSearchResultOptional.get();
    this.currentIndex = componentSearchResult.getIndex() + 1;
    this.currentStatementParents = componentSearchResult.getParents();
    this.updateCurrentComponents();
    this.path = path;
    this.predicates = (path.states().isEmpty())
      ? new HashMap<>()
      : path.states().getLast().getPredicates().orElse(new HashSet<>()).stream()
      .collect(Collectors.toMap(Predicate::getUuid, predicate -> predicate));
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

      return getNextComponent(
        parentStatement.getIndex(),
        parentStatement.getComponents(),
        new LinkedList<>(((LinkedList<ParentStatement>) currentStatementParents).subList(
          0,
          currentStatementParents.size() - 1
        ))
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
        this.predicates,
        this.predicateValuations,
        expression.toFormula(this.variables)
      ).get());
    } catch (InterruptedException | ExecutionException e) {
      return Optional.empty();
    }
  }

  private void handleControlStatement(ControlStatement controlStatement, AbstractLiteral abstractLiteral) {
    switch (abstractLiteral) {
      case TRUE -> {
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
        if (controlStatement instanceof WhileStatement) {
          this.currentIndex++;
          return;
        }

        if (((IfStatement) controlStatement).getElseBody() == null) {
          this.currentIndex++;
          return;
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

        this.modelCheckingNode.sendMessage(new Message<>(new DistributeModelCheckingRequest(
          new Path(Stream.concat(
            this.path.states().stream(),
            Stream.of(new State(new Location(bodyComponent.getLine(), bodyComponent.getLinePosition())))
          ).toList()),
          new ArrayList<>(List.of(this.predicateValuations))
        ), new Recipient(NodeType.MODEL_CHECKING_GATEWAY)));

        this.handleControlStatement(controlStatement, AbstractLiteral.FALSE);
      }
    }
  }

  private void handleCurrentParentStatement() {
    if (this.currentStatementParents.isEmpty()) {
      return;
    }

    ParentStatement parentStatement = ((LinkedList<ParentStatement>) this.currentStatementParents).pop();

    if (parentStatement.getControlStatement() instanceof WhileStatement) {
      Optional<AbstractLiteral> abstractionLiteralOptional = this.evaluateExpression(
        parentStatement.getControlStatement().getConditionExpression()
      );

      if (abstractionLiteralOptional.isEmpty()) {
        return;
      }

      AbstractLiteral abstractLiteral = abstractionLiteralOptional.get();
      this.handleControlStatement(parentStatement.getControlStatement(), abstractLiteral);
      return;
    }

    this.updateCurrentComponents();
    this.currentIndex = parentStatement.getIndex() + 1;
  }

  private void handleAssignment(String variable, Expression expression) {
    Optional<Component> nextComponentOptional = this.getNextComponent();

    if (nextComponentOptional.isEmpty()) {
      return;
    }

    Formula expressionFormula = expression.toFormula(this.variables);
    Set<String> expressionRelevantPredicates = expressionFormula.getReferencedVariables();
    Set<Predicate> relevantPredicates = this.predicates.values().stream()
      .filter(predicate -> predicate.getFormula().getReferencedVariables().stream()
        .anyMatch(expressionRelevantPredicates::contains))
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
          this.predicates,
          this.predicateValuations,
          predicate.getFormula())))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    Map<UUID, AbstractLiteral> predicateAbstractLiteral = new HashMap<>();

    HashMap<UUID, PredicateValuation> deterministicPredicateValuations = new HashMap<>();
    List<UUID> nonDeterministicPredicateValuations = new ArrayList<>();
    for (Map.Entry<UUID, CompletableFuture<AbstractLiteral>> predicateCompletableFuture : predicateCompletableFutures.entrySet()) {
      try {
        AbstractLiteral abstractLiteral = predicateCompletableFuture.getValue().get();
        predicateAbstractLiteral.put(predicateCompletableFuture.getKey(), abstractLiteral);
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

    List<Map<UUID, PredicateValuation>> predicateValuations = PredicateValuation.getCombinations(
      deterministicPredicateValuations,
      nonDeterministicPredicateValuations
    );

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
    predicateValuations.removeFirst();

    Component nextComponent = nextComponentOptional.get();
    this.modelCheckingNode.sendMessage(new Message<>(
      new DistributeModelCheckingRequest(new Path(Stream.concat(
        this.path.states().stream(),
        Stream.of(new State(
          new Location(nextComponent.getLine(), nextComponent.getLinePosition()),
          new HashSet<>(this.predicates.values())
        ))
      ).toList()), predicateValuations),
      new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
    ));
  }

  public Optional<Path> check() {
    while (true) {
      if (this.currentIndex >= this.currentComponents.size()) {
        if (this.currentStatementParents.isEmpty()) {
          return Optional.of(new Path(new ArrayList<>(List.of(new State(new Location(-1, -1))))));
        }

        this.handleCurrentParentStatement();
        continue;
      }

      Component component = this.currentComponents.get(this.currentIndex);
      this.path.states().add(new State(new Location(component.getLine(), component.getLinePosition())));

      switch (component) {
        case ControlStatement controlStatement -> {
          System.out.println("control statement");
          Optional<AbstractLiteral> abstractionLiteralOptional = this.evaluateExpression(
            controlStatement.getConditionExpression()
          );

          if (abstractionLiteralOptional.isEmpty()) {
            return Optional.empty();
          }

          this.handleControlStatement(controlStatement, abstractionLiteralOptional.get());
          continue;
        }
        // case Input input -> {}
        case Output output -> {
          output.getDeclarationExpression().ifPresent(expression -> this.handleAssignment(
            output.getId(),
            expression
          ));
        }
        case DeclarationVariableStatement declarationVariableStatement -> {
          System.out.println("declaration statement");
          declarationVariableStatement.getDeclarationExpression().ifPresent(expression -> this.handleAssignment(
            declarationVariableStatement.getId(),
            expression
          ));
        }
        case AssignmentStatement assignmentStatement -> {
          System.out.println("assignment statement");
          this.handleAssignment(assignmentStatement.getId(), assignmentStatement.getAssignExpression());
        }
        case AssertStatement assertStatement -> {
          System.out.println("assert statement");
          Optional<AbstractLiteral> abstractLiteralOptional = this.evaluateExpression(assertStatement.getExpression());

          if (abstractLiteralOptional.isEmpty()) {
            return Optional.empty();
          }

          if (abstractLiteralOptional.get() != AbstractLiteral.FALSE) {
            return Optional.of(this.path);
          }
        }
        default -> {
          this.modelCheckingNode.sendMessage(new Message<>(
            new UnsupportedComponent(this.modelCheckingNode.getUuid(), component.getClass().getSimpleName()),
            new Recipient(NodeType.ROOT)
          ));
          return Optional.empty();
        }
      }

      this.currentIndex++;
    }
  }
}
