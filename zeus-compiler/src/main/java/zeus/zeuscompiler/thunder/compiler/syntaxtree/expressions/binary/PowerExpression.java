package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary;

import zeus.shared.formula.Formula;
import zeus.shared.formula.binary.PowerFormula;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.TypeCheckingUtils;

import java.util.List;
import java.util.Optional;

public class PowerExpression extends BinaryExpression {
  public PowerExpression(
    int line,
    int linePosition,
    Expression leftExpression,
    Expression rightExpression,
    BinaryExpressionType type
  ) {
    super(line, linePosition, leftExpression, rightExpression, type);
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    return TypeCheckingUtils.evaluateTypeNumericExpression(this);
  }

  @Override
  public Formula toFormula() {
    return new PowerFormula(this.leftExpression.toFormula(), this.rightExpression.toFormula());
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s ** %s",
        this.leftExpression.translate(depth, exportTarget),
        this.rightExpression.translate(depth, exportTarget)
      );
    };
  }
}
