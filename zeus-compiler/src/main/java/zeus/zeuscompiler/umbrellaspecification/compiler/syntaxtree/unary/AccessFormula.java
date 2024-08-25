package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;

import java.util.List;

public class AccessFormula extends UnaryFormula {
  public AccessFormula(int line, int linePosition, Formula formula) {
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
