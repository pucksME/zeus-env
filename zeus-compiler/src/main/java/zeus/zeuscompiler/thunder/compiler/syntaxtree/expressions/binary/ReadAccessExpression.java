package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.MapType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.ArrayList;
import java.util.List;
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
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> containerExpressionTypeOptional = this.leftExpression.evaluateType(symbolTable, compilerErrors);

    if (containerExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> accessExpressionTypeOptional = this.rightExpression.evaluateType(symbolTable, compilerErrors);

    if (accessExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type containerExpressionType = containerExpressionTypeOptional.get();
    Type accessExpressionType = accessExpressionTypeOptional.get();

    if (!(containerExpressionType instanceof ListType) && !(containerExpressionType instanceof MapType)) {
      compilerErrors.add(new CompilerError(
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
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    if (containerExpressionType instanceof MapType &&
      !((MapType) containerExpressionType).getKeyType().equals(accessExpressionType)) {
      compilerErrors.add(new CompilerError(
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
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        Optional<Type> typeOptional = this.leftExpression.evaluateType(symbolTable, new ArrayList<>());

        if (typeOptional.isPresent() && typeOptional.get() instanceof MapType) {
          yield String.format(
            "%s.get(%s)",
            this.leftExpression.translate(symbolTable, depth, exportTarget),
            this.rightExpression.translate(symbolTable, depth, exportTarget)
          );
        }

        yield String.format(
          "%s[%s]",
          this.leftExpression.translate(symbolTable, depth, exportTarget),
          this.rightExpression.translate(symbolTable, depth, exportTarget)
        );
      }
    };
  }
}
