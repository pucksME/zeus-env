package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.shared.message.payload.modelchecking.Location;
import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.compiler.utils.ParentStatement;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public abstract class Component extends TypeCheckableNode implements Translatable {
  public Component(int line, int linePosition) {
    super(line, linePosition);
  }

  public Optional<ComponentSearchResult> searchComponent(Location location, int index, Queue<ParentStatement> parents) {
    if (this.getLine() == location.line() && this.getLinePosition() == location.linePosition()) {
      return Optional.of(new ComponentSearchResult(this, index, new LinkedList<>()));
    }
    return Optional.empty();
  }
}
