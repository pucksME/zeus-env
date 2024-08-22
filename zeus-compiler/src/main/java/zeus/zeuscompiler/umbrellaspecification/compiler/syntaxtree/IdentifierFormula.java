package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;

import java.util.List;

public class IdentifierFormula extends Formula {
  public IdentifierFormula(int line, int linePosition) {
    super(line, linePosition);
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
