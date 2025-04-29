package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.*;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import java.util.Optional;

public class ReadAccessExpression extends BinaryExpression {
  public ReadAccessExpression(
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
    Optional<Type> containerExpressionTypeOptional = this.leftExpression.evaluateType();

    if (containerExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> accessExpressionTypeOptional = this.rightExpression.evaluateType();

    if (accessExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type containerExpressionType = containerExpressionTypeOptional.get();
    Type accessExpressionType = accessExpressionTypeOptional.get();

    if (containerExpressionType instanceof IdType) {
      ServerRouteSymbolTable serverRouteSymbolTable = ServiceProvider.provide(SymbolTableService.class).getContextSymbolTableProvider().provide(ServerRouteSymbolTable.class);
      Optional<TypeInformation> typeInformationOptional = serverRouteSymbolTable.getType(serverRouteSymbolTable.getCurrentCodeModule(), ((IdType) containerExpressionType).getId());
      if (typeInformationOptional.isPresent()) {
        containerExpressionType = typeInformationOptional.get().getType();
      }
    }

    if (!(containerExpressionType instanceof ListType) && !(containerExpressionType instanceof MapType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    if (containerExpressionType instanceof ListType &&
      (!(accessExpressionType instanceof PrimitiveType) ||
        ((PrimitiveType) accessExpressionType).getType() != LiteralType.INT)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    if (containerExpressionType instanceof MapType &&
      !((MapType) containerExpressionType).getKeyType().equals(accessExpressionType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of((containerExpressionType instanceof ListType)
      ? ((ListType) containerExpressionType).getType()
      : ((MapType) containerExpressionType).getValueType());
  }

  @Override
  public Expr toFormula(Context context) {
    throw new RuntimeException("Could not convert read access expression to formula: not implemented yet");
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        Optional<Type> typeOptional = this.leftExpression.evaluateType();

        if (typeOptional.isPresent() && typeOptional.get() instanceof MapType) {
          yield String.format(
            "%s.get(%s)",
            this.leftExpression.translate(depth, exportTarget),
            this.rightExpression.translate(depth, exportTarget)
          );
        }

        yield String.format(
          "%s[%s]",
          this.leftExpression.translate(depth, exportTarget),
          this.rightExpression.translate(depth, exportTarget)
        );
      }
    };
  }
}
