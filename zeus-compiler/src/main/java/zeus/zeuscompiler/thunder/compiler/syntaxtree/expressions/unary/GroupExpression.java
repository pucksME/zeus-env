package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import zeus.shared.formula.Formula;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

import java.util.Map;
import java.util.Optional;

public class GroupExpression extends UnaryExpression {

  public GroupExpression(int line, int linePosition, Expression expression, UnaryExpressionType unaryExpressionType) {
    super(line, linePosition, expression, unaryExpressionType);
    this.expression = expression;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    return this.expression.evaluateType();
  }

  @Override
  public Formula toFormula(Map<String, VariableInformation> variables) {
    return this.expression.toFormula(variables);
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "(%s)",
        this.expression.translate(depth, exportTarget)
      );
    };
  }
}
