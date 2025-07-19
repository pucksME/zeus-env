package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Optional;

public class AssertStatement extends Statement {
  Expression expression;

  public AssertStatement(int line, int linePosition, Expression expression) {
    super(line, linePosition);
    this.expression = expression;
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format("assert(%s);", this.expression.translate(depth, exportTarget));
    };
  }

  @Override
  public void checkTypes() {
    Optional<Type> typeOptional = this.expression.evaluateType();

    if (typeOptional.isEmpty()) {
      return;
    }

    Type type = typeOptional.get();

    if (!(type instanceof PrimitiveType) || ((PrimitiveType) type).getType() != LiteralType.BOOLEAN) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.expression.getLine(),
        this.expression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  public Expression getExpression() {
    return expression;
  }
}
