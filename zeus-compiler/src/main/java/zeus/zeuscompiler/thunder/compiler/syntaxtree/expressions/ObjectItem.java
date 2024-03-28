package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

public class ObjectItem {
  String id;
  Expression expression;

  public ObjectItem(String id, Expression expression) {
    this.id = id;
    this.expression = expression;
  }
}
