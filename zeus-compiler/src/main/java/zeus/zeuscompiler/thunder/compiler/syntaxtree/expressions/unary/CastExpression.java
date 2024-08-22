package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
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
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> expressionTypeOptional = this.expression.evaluateType(symbolTable, compilerErrors);

    if (expressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    if (!expressionTypeOptional.get().compatible(symbolTable, compilerErrors, this.type)) {
      compilerErrors.add(new CompilerError(
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
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s as %s",
        this.expression.translate(symbolTable, depth, exportTarget),
        this.type.translate(symbolTable, depth, exportTarget)
      );
    };
  }
}
