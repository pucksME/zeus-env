package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;

import java.util.List;

public class TemporalUnaryFormula extends UnaryFormula {
  public TemporalUnaryFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition, formula);
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
