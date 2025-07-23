package zeus.shared.formula.binary;

import zeus.shared.formula.Formula;

import java.util.HashSet;
import java.util.Set;

public abstract class BinaryArithmeticFormula extends BinaryFormula {
  public BinaryArithmeticFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public boolean isAtomic() {
    return true;
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    return new HashSet<>(Set.of(this));
  }
}
