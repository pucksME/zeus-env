package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class NegateExpression extends UnaryExpression {
  public NegateExpression(int line, int linePosition, Expression expression, UnaryExpressionType type) {
    super(line, linePosition, expression, type);
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

    Type expressionType = expressionTypeOptional.get();

    if (!(expressionType instanceof PrimitiveType) ||
      ((PrimitiveType) expressionType).getType() != LiteralType.BOOLEAN) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }
    return Optional.of(expressionType);
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "!%s",
        this.expression.translate(depth, exportTarget)
      );
    };
  }
}
