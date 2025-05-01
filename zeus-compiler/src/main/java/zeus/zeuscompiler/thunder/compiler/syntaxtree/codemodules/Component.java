package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.shared.message.payload.modelchecking.Location;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;

import java.util.Optional;

public abstract class Component extends TypeCheckableNode {
  public Component(int line, int linePosition) {
    super(line, linePosition);
  }

  public Optional<Component> findComponent(Location location) {
    if (this.getLine() == location.line() && this.getLinePosition() == location.linePosition()) {
      return Optional.of(this);
    }
    return Optional.empty();
  }
}
