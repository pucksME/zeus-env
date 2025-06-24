package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.modelchecking.ExpressionVariableInformationNotPresent;
import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.message.payload.modelchecking.UnsupportedComponent;
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

public class CodeModuleModelChecker {
  ClientCodeModule codeModule;
  ModelCheckingNode modelCheckingNode;
  int codeModuleSearchIndex;
  Queue<ParentStatement> currentStatementParents;
  List<Component> currentComponents;
  int currentIndex;
  boolean assertionViolationReached;
  ExecutorService abstractionExecutorService;
  HashMap<UUID, Predicate> predicates;
  HashMap<UUID, PredicateValuation> predicateValuations;

  public CodeModuleModelChecker(ClientCodeModule codeModule, ModelCheckingNode modelCheckingNode) {
    this.codeModule = codeModule;
    this.modelCheckingNode = modelCheckingNode;
    this.currentStatementParents = null;
    this.currentComponents = new ArrayList<>();
    this.currentIndex = 0;
    this.assertionViolationReached = false;
    this.abstractionExecutorService = Executors.newSingleThreadExecutor();
    this.predicates = new HashMap<>();
    this.predicateValuations = new HashMap<>();
  }

  private void updateCurrentComponents() {
    this.currentComponents = this.currentStatementParents.isEmpty()
      ? this.codeModule.getComponents()
      : ((LinkedList<ParentStatement>) this.currentStatementParents).peekLast().getComponents();
  }

  public boolean calibrate(Path path) {
    Optional<ComponentSearchResult> componentSearchResultOptional = path.states().isEmpty()
      ? this.codeModule.getFirstComponent()
      : this.codeModule.searchComponent(path.states().getLast().location());

    if (componentSearchResultOptional.isEmpty()) {
      return false;
    }

    ComponentSearchResult componentSearchResult = componentSearchResultOptional.get();
    this.codeModuleSearchIndex = componentSearchResult.getIndex();
    this.currentIndex = this.codeModuleSearchIndex;
    this.currentStatementParents = componentSearchResult.getParents();
    this.updateCurrentComponents();
    return true;
  }

  private Optional<AbstractLiteral> evaluateExpression(Expression expression) {
    Optional<Map<String, VariableInformation>> variablesOptional = this.codeModule.getVariables();
    if (variablesOptional.isEmpty()) {
      this.modelCheckingNode.sendMessage(new Message<>(
        new ExpressionVariableInformationNotPresent(
          this.modelCheckingNode.getUuid(),
          expression.getLine(),
          expression.getLinePosition()
        ),
        new Recipient(NodeType.ROOT)
      ));
      return Optional.empty();
    }

    try {
      return Optional.of(this.modelCheckingNode.sendAbstractRequest(
        this.predicates,
        this.predicateValuations,
        expression.toFormula(variablesOptional.get())
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
        // TODO: distribute one
        //this.handleControlStatement(controlStatement, AbstractionLiteral.TRUE);
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

  public Optional<Path> check() {
    while (true) {
      if (this.currentIndex >= this.currentComponents.size()) {
        if (this.currentStatementParents.isEmpty()) {
          return Optional.empty();
        }

        this.handleCurrentParentStatement();
        continue;
      }

      Component component = this.currentComponents.get(this.currentIndex);

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
        case Input input -> {}
        case Output output -> {}
        case DeclarationVariableStatement declarationVariableStatement -> {
          System.out.println("declaration statement");
        }
        case AssignmentStatement assignmentStatement -> {
          System.out.println("assignment statement");
        }
        case AssertStatement assertStatement -> {
          System.out.println("assert statement");
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
