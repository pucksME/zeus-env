package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.Optional;

public class LiteralFormula extends Formula {
  public LiteralFormula(int line, int linePosition) {
    super(line, linePosition);
  }

  @Override
  public Optional<Type> evaluateType() {
    return Optional.empty();
  }

  @Override
  public void check() {

  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }
}
