package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;

public abstract class UnaryExpression extends Expression {
  Expression expression;
  UnaryExpressionType type;

  public UnaryExpression(int line, int linePosition, Expression expression, UnaryExpressionType type) {
    super(line, linePosition);
    this.expression = expression;
    this.type = type;
  }
}
