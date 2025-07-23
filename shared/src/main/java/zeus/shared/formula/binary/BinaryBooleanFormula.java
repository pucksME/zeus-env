package zeus.shared.formula.binary;

import zeus.shared.formula.Formula;

public abstract class BinaryBooleanFormula extends BinaryFormula {
  public BinaryBooleanFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public boolean isBoolean() {
    return true;
  }

  @Override
  public boolean isAtomic() {
    return false;
  }
}
