package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.rain.dtos.ExportTarget;


public class IdentifierFormula extends Formula {
  String id;

  public IdentifierFormula(int line, int linePosition, String id) {
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

  public String getId() {
    return id;
  }
}
