package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class CastExpression extends UnaryExpression {
  Type type;

  public CastExpression(
    int line,
    int linePosition,
    Type type,
    Expression expression,
    UnaryExpressionType unaryExpressionType
  ) {
    super(line, linePosition, expression, unaryExpressionType);
    this.type = type;
    this.expression = expression;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<Type> expressionTypeOptional = this.expression.evaluateType();

    if (expressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    if (!expressionTypeOptional.get().compatible(this.type)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(this.type);
  }

  @Override
  public Expr toFormula(Context context) {
    return this.expression.toFormula(context);
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s as %s",
        this.expression.translate(depth, exportTarget),
        this.type.translate(depth, exportTarget)
      );
    };
  }
}
