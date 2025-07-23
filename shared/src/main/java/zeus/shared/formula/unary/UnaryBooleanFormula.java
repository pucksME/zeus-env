package zeus.shared.formula.unary;

import zeus.shared.formula.Formula;

public abstract class UnaryBooleanFormula extends UnaryFormula {
  public UnaryBooleanFormula(Formula formula) {
    super(formula);
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
