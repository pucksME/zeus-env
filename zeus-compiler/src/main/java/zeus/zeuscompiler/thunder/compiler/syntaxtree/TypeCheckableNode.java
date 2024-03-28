package zeus.zeuscompiler.thunder.compiler.syntaxtree;

import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.CompilerError;

import java.util.List;

public abstract class TypeCheckableNode extends Node {
  public TypeCheckableNode() {
    super(-1, -1);
  }

  public TypeCheckableNode(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors);
}
