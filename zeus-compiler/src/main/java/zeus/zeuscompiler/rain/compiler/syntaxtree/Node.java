package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;

public abstract class Node implements Translatable {
  int line;
  int linePosition;
  String name;

  public Node(int line, int linePosition, String name) {
    this.line = line;
    this.linePosition = linePosition;
    this.name = name;
  }

  public abstract void check(SymbolTable symbolTable, List<CompilerError> compilerErrors);

  public int getLine() {
    return line;
  }

  public int getLinePosition() {
    return linePosition;
  }

  public String getName() {
    return name;
  }
}
