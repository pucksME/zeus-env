package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.AssignmentStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.ControlStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.DeclarationVariableStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.WhileStatement;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.compiler.utils.ParentStatement;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CodeModuleIterator implements Iterator<Set<Path>> {
  ClientCodeModule codeModule;
  ModelCheckingNode modelCheckingNode;
//  Set<Predicate> predicates;
  Queue<ParentStatement> currentStatementParents;
  List<Component> currentComponents;
  int currentIndex;
  boolean assertionViolationReached;
  ExecutorService abstractionExecutorService;

  public CodeModuleIterator(ClientCodeModule codeModule, ModelCheckingNode modelCheckingNode) {
    this.codeModule = codeModule;
    this.modelCheckingNode = modelCheckingNode;
//    this.predicates = new HashSet<>();
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
    this.currentIndex = componentSearchResult.getIndex();
    this.currentStatementParents = componentSearchResult.getParents();
//    this.predicates = new HashSet<>(predicates);
    this.updateCurrentComponents();
    return true;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  private void handleComponent(Component component) {
    if (component instanceof Input input) {
      return;
    }

    if (component instanceof Output output) {
      return;
    }

    if (component instanceof DeclarationVariableStatement declarationVariableStatement) {
      return;
    }

    if (component instanceof AssignmentStatement assignmentStatement) {
      return;
    }

    if (component instanceof ControlStatement controlStatement) {
      return;
    }
  }

  @Override
  public Set<Path> next() {
    if (currentIndex >= this.currentComponents.size() - 1) {
      if (!this.currentStatementParents.isEmpty()) {
        if (!(this.currentStatementParents.peek().getControlStatement() instanceof WhileStatement)) {
          this.currentStatementParents.poll();
        }
      }

      this.updateCurrentComponents();
      this.currentIndex = 0;
      return Set.of();
    }

    Component component = this.currentComponents.get(currentIndex);
    this.currentIndex++;
    return Set.of();
  }
}
