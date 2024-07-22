package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Formula;

import java.util.List;

public class TemporalUnaryFormula extends UnaryFormula {
  public TemporalUnaryFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition, formula);
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
