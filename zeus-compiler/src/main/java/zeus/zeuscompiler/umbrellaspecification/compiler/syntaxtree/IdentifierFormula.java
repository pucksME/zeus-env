package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;

public class IdentifierFormula extends Formula {
  public IdentifierFormula(int line, int linePosition) {
    super(line, linePosition);
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
