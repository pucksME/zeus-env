package zeus.zeuscompiler.thunder.compiler.syntaxtree;

public abstract class Node {
  int line;
  int linePosition;

  public Node(int line, int linePosition) {
    this.line = line;
    this.linePosition = linePosition;
  }

  public int getLine() {
    return line;
  }

  public int getLinePosition() {
    return linePosition;
  }
}
