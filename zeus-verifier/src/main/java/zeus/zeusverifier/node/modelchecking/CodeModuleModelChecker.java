package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.payload.modelchecking.Path;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.WhileStatement;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.compiler.utils.ParentStatement;
import zeus.shared.message.payload.abstraction.AbstractionLiteral;

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

  public CodeModuleModelChecker(ClientCodeModule codeModule, ModelCheckingNode modelCheckingNode) {
    this.codeModule = codeModule;
    this.modelCheckingNode = modelCheckingNode;
    this.currentStatementParents = null;
    this.currentComponents = new ArrayList<>();
    this.currentIndex = 0;
    this.assertionViolationReached = false;
    this.abstractionExecutorService = Executors.newSingleThreadExecutor();
  }

  private void updateCurrentComponents() {
    this.currentComponents = this.currentStatementParents.isEmpty()
      ? this.codeModule.getComponents()
      : this.currentStatementParents.peek().getComponents();
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

  private void handleCurrentParentStatement() {
    if (this.currentStatementParents.isEmpty()) {
      return;
    }

    ParentStatement parentStatement = this.currentStatementParents.peek();
    AbstractionLiteral conditionEvaluation = null;

    try {
      conditionEvaluation = this.modelCheckingNode.sendAbstractRequest().get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    if (parentStatement.getControlStatement() instanceof WhileStatement) {
      if (conditionEvaluation == AbstractionLiteral.NON_DETERMINISTIC) {
        // TODO: check if already visited
        // TODO: if already visited, do not distribute and handle like false
        this.currentIndex = 0;
      }

      if (conditionEvaluation == AbstractionLiteral.TRUE) {
        // TODO: check if already visited
        this.currentIndex = 0;
        return;
      }
    }

    this.currentStatementParents.poll();
    this.updateCurrentComponents();

    this.currentIndex = this.currentStatementParents.isEmpty()
      ? this.codeModuleSearchIndex
      : this.currentStatementParents.peek().getIndex();

  }

  public Optional<Path> check() {
    try {
      AbstractionLiteral abstractionLiteral = this.modelCheckingNode.sendAbstractRequest().get();
      System.out.printf("abstraction response: \"%s\"%n", abstractionLiteral);
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }

    while (true) {
      if (this.currentIndex >= this.currentComponents.size() - 1) {
        if (this.currentStatementParents.isEmpty()) {
          return Optional.empty();
        }

        this.handleCurrentParentStatement();
        continue;
      }
      this.currentIndex++;
    }
  }
}
