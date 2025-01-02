package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.List;
import java.util.Optional;

public class QuantifierFormula extends BinaryFormula {
  String id;
  QuantifierFormulaType type;

  public QuantifierFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    String id,
    QuantifierFormulaType type
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.id = id;
    this.type = type;
  }

  @Override
  public Optional<Type> evaluateType() {
    return Optional.empty();
  }

  @Override
  public String translate() {
    return "";
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return "";
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return "";
  }

  @Override
  public void check() {
  }
}
