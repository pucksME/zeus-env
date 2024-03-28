package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;

public abstract class Statement extends BodyComponent {
  public Statement(int line, int linePosition) {
    super(line, linePosition);
  }
}
