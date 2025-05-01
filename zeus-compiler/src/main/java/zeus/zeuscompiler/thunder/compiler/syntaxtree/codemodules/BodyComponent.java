package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.Translatable;

public abstract class BodyComponent extends Component implements Translatable {
  public BodyComponent(int line, int linePosition) {
    super(line, linePosition);
  }
}
