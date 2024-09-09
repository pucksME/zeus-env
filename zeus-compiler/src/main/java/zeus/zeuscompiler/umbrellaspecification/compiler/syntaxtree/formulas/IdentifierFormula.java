package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.Optional;


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

  @Override
  public Optional<Type> evaluateType() {
    return Optional.empty();
  }
}
