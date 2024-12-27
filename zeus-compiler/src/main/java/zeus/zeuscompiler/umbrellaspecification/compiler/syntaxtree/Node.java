package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;

import java.util.List;

public abstract class Node {
  int line;
  int linePosition;

  public Node(int line, int linePosition) {
    this.line = line;
    this.linePosition = linePosition;
  }

  public abstract void check();

  public abstract boolean accessesResponse();

  public int getLine() {
    return line;
  }

  public int getLinePosition() {
    return linePosition;
  }
}
