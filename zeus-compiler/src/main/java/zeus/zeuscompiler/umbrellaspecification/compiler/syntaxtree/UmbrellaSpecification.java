package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.rain.dtos.ExportTarget;

import java.util.List;

public class UmbrellaSpecification extends Node {
  String id;
  Formula formula;
  Context context;
  Action action;

  public UmbrellaSpecification(int line, int linePosition, String id) {
    super(line, linePosition);
    this.id = id;
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }

  public void setFormula(Formula formula) {
    this.formula = formula;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public void setAction(Action action) {
    this.action = action;
  }
}
