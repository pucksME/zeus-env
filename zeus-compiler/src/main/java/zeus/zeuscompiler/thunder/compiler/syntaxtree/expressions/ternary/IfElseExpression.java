package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.ternary;

import zeus.shared.formula.Formula;
import zeus.shared.formula.binary.AndFormula;
import zeus.shared.formula.binary.OrFormula;
import zeus.shared.formula.unary.NotFormula;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;

public class IfElseExpression extends TernaryExpression {
  public IfElseExpression(
    int line,
    int linePosition,
    Expression firstExpression,
    Expression secondExpression,
    Expression thirdExpression,
    TernaryExpressionType type
  ) {
    super(line, linePosition, firstExpression, secondExpression, thirdExpression, type);
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "(%s) ? %s : %s",
        this.firstExpression.translate(depth, exportTarget),
        this.secondExpression.translate(depth, exportTarget),
        this.thirdExpression.translate(depth, exportTarget)
      );
    };
  }

  @Override
  public Formula toFormula() {
    Formula condition = this.firstExpression.toFormula();
    return new OrFormula(
      new AndFormula(condition, this.secondExpression.toFormula()),
      new AndFormula(new NotFormula(condition), this.thirdExpression.toFormula()));
  }
}
