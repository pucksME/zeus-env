package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ListExpression extends Expression {
  List<Expression> expressions;

  public ListExpression(int line, int linePosition, List<Expression> expressions) {
    super(line, linePosition);
    this.expressions = expressions;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    Type type = null;
    for (Expression expression : this.expressions) {
      Optional<Type> currentTypeOptional = expression.evaluateType();

      if (currentTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      Type currentType = currentTypeOptional.get();

      if (type != null && !currentType.equals(type)) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          expression.getLine(),
          expression.getLinePosition(),
          new IncompatibleTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      type = currentType;
    }

    return Optional.of(new ListType(type, this.expressions.size()));
  }

  @Override
  public Expr toFormula(Context context) {
    throw new RuntimeException("Could not convert list expression to formula: not implemented yet");
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "[%s]",
        this.expressions.stream().map(
          expression -> expression.translate(depth, exportTarget)
        ).collect(Collectors.joining(", "))
      );
    };
  }
}
