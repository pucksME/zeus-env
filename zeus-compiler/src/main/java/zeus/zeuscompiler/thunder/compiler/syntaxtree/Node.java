package zeus.zeuscompiler.thunder.compiler.syntaxtree;

public abstract class Node {
  int line;
  int linePosition;
  final String className;

  public Node(int line, int linePosition) {
    this.line = line;
    this.linePosition = linePosition;
    this.className = this.getClass().getName();
  }

  public int getLine() {
    return line;
  }

  public int getLinePosition() {
    return linePosition;
  }
}
