package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;

import java.util.List;

public class LiteralFormula extends Formula {
  public LiteralFormula(int line, int linePosition) {
    super(line, linePosition);
  }

  @Override
  public void check() {

  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }
}
