package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

public abstract class Node {
  int line;
  int linePosition;

  public Node(int line, int linePosition) {
    this.line = line;
    this.linePosition = linePosition;
  }
}
