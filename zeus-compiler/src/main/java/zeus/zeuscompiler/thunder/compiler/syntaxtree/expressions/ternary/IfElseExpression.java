package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.ternary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;

public class IfElseExpression extends TernaryExpression {
  public IfElseExpression(
    int line,
    int linePosition,
    Expression firstExpression,
    Expression secondExpression,
    Expression thirdExpression,
    TernaryExpressionType type
  ) {
    super(line, linePosition, firstExpression, secondExpression, thirdExpression, type);
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "(%s) ? %s : %s",
        this.firstExpression.translate(symbolTable, depth, exportTarget),
        this.secondExpression.translate(symbolTable, depth, exportTarget),
        this.thirdExpression.translate(symbolTable, depth, exportTarget)
      );
    };
  }
}
