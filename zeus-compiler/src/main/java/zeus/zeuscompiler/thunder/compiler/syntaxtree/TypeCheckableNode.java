package zeus.zeuscompiler.thunder.compiler.syntaxtree;

import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.CompilerError;

import java.util.List;

public abstract class TypeCheckableNode extends Node {
  public TypeCheckableNode() {
    super(-1, -1);
  }

  public TypeCheckableNode(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract void checkTypes();
}
