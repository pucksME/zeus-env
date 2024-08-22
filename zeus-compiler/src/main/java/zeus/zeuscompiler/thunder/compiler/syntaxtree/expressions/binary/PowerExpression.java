package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.TypeCheckingUtils;

import java.util.List;
import java.util.Optional;

public class PowerExpression extends BinaryExpression {
  public PowerExpression(
    int line,
    int linePosition,
    Expression leftExpression,
    Expression rightExpression,
    BinaryExpressionType type
  ) {
    super(line, linePosition, leftExpression, rightExpression, type);
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    return TypeCheckingUtils.evaluateTypeNumericExpression(symbolTable, compilerErrors, this);
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s ** %s",
        this.leftExpression.translate(symbolTable, depth, exportTarget),
        this.rightExpression.translate(symbolTable, depth, exportTarget)
      );
    };
  }
}
