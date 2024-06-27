package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;

public abstract class Node {
  int line;
  int linePosition;

  public Node(int line, int linePosition) {
    this.line = line;
    this.linePosition = linePosition;
  }

  public abstract void check(SymbolTable symbolTable, List<CompilerError> compilerErrors);
}
