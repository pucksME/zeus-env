package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;

import java.util.List;

public class LogicalBinaryFormula extends BinaryFormula {
  public LogicalBinaryFormula(int line, int linePosition, Formula leftFormula, Formula rightFormula) {
    super(line, linePosition, leftFormula, rightFormula);
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
