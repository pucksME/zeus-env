package zeus.shared.formula.unary;

import zeus.shared.formula.Formula;

public abstract class UnaryFormula extends Formula {
  Formula formula;

  public UnaryFormula(Formula formula) {
    this.formula = formula;
  }
}
