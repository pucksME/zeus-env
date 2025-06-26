package zeus.shared.formula.unary;

import zeus.shared.formula.Formula;

import java.util.Set;

public abstract class UnaryFormula extends Formula {
  Formula formula;

  public UnaryFormula(Formula formula) {
    this.formula = formula;
  }

  @Override
  public Set<String> getReferencedVariables() {
    return formula.getReferencedVariables();
  }
}
