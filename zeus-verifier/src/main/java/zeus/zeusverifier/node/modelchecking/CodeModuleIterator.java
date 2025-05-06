package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.compiler.utils.ParentStatement;

import java.util.*;

public class CodeModuleIterator implements Iterator<Set<Path>> {
  ClientCodeModule codeModule;
  Set<Predicate> predicates;
  Queue<ParentStatement> currentStatementParents;
  int currentIndex;

  public CodeModuleIterator(ClientCodeModule codeModule) {
    this.codeModule = codeModule;
    this.predicates = new HashSet<>();
    this.currentStatementParents = null;
    this.currentIndex = 0;
  }

  public boolean calibrate(Path path) {
    Optional<ComponentSearchResult> componentSearchResultOptional = path.locations().isEmpty()
      ? this.codeModule.getFirstComponent()
      : this.codeModule.searchComponent(path.locations().getLast());

    if (componentSearchResultOptional.isEmpty()) {
      return false;
    }

    ComponentSearchResult componentSearchResult = componentSearchResultOptional.get();
    this.currentIndex = componentSearchResult.getIndex();
    this.currentStatementParents = componentSearchResult.getParents();
    this.predicates = new HashSet<>(predicates);
    return true;
  }

  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public Set<Path> next() {
    return Set.of();
  }
}
