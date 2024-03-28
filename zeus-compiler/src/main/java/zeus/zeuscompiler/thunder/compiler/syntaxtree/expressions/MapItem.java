package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

public class MapItem {
  Expression key;
  Expression value;

  public MapItem(Expression key, Expression value) {
    this.key = key;
    this.value = value;
  }
}
