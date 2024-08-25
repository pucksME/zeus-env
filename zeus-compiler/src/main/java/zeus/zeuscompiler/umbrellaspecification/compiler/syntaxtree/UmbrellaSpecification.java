package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
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
  public void check() {
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
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
