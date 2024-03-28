package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;

public abstract class BodyComponent extends TypeCheckableNode implements Translatable {
  public BodyComponent(int line, int linePosition) {
    super(line, linePosition);
  }
}
