package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;

public abstract class ControlStatement extends Statement {
  Expression conditionExpression;

  public ControlStatement(int line, int linePosition, Expression conditionExpression) {
    super(line, linePosition);
    this.conditionExpression = conditionExpression;
  }
}
