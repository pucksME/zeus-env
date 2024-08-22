package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;

import java.util.List;

public class CompareBinaryFormula extends BinaryFormula {
  public CompareBinaryFormula(int line, int linePosition, Formula leftFormula, Formula rightFormula) {
    super(line, linePosition, leftFormula, rightFormula);
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
