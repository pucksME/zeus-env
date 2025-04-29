package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;

import java.util.List;
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
  public Expr toFormula(Context context) {
    return this.expression.toFormula(context);
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
