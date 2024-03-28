package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;

public abstract class BinaryExpression extends Expression {
  Expression leftExpression;
  Expression rightExpression;
  BinaryExpressionType type;

  public BinaryExpression(
    int line,
    int linePosition,
    Expression leftExpression,
    Expression rightExpression,
    BinaryExpressionType type
  ) {
    super(line, linePosition);
    this.leftExpression = leftExpression;
    this.rightExpression = rightExpression;
    this.type = type;
  }

  public Expression getLeftExpression() {
    return leftExpression;
  }

  public Expression getRightExpression() {
    return rightExpression;
  }

  public BinaryExpressionType getType() {
    return type;
  }
}
