package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.TypeCheckingUtils;

import java.util.List;
import java.util.Optional;

public class MultiplyExpression extends BinaryExpression {
  public MultiplyExpression(
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
    return TypeCheckingUtils.evaluateTypeNumericExpression(symbolTable, compilerErrors, this);
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s * %s",
        this.leftExpression.translate(symbolTable, depth, exportTarget),
        this.rightExpression.translate(symbolTable, depth, exportTarget)
      );
    };
  }
}
