package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.unary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;


public class LogicalNotFormula extends UnaryFormula {
  public LogicalNotFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition, formula);
  }

  @Override
  public void check() {
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }
}
