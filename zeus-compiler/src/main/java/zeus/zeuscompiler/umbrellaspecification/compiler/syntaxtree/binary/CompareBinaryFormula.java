package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;

import java.util.List;

public class CompareBinaryFormula extends BinaryFormula {
  CompareBinaryFormulaType compareBinaryFormulaType;

  public CompareBinaryFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    CompareBinaryFormulaType compareBinaryFormulaType
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.compareBinaryFormulaType = compareBinaryFormulaType;
  }

  @Override
  public void check() {

  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }
}
