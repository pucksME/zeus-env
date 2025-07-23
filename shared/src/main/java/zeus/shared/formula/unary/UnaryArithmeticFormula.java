package zeus.shared.formula.unary;

import zeus.shared.formula.Formula;

public abstract class UnaryArithmeticFormula extends UnaryFormula {
  public UnaryArithmeticFormula(Formula formula) {
    super(formula);
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public boolean isAtomic() {
    return true;
  }
}
