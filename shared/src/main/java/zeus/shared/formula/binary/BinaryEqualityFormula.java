package zeus.shared.formula.binary;

import zeus.shared.formula.Formula;

import java.util.HashSet;
import java.util.Set;

public abstract class BinaryEqualityFormula extends BinaryFormula{
  public BinaryEqualityFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public boolean isBoolean() {
    return this.leftFormula.isBoolean() || this.rightFormula.isBoolean();
  }

  @Override
  public boolean isAtomic() {
    return this.isBoolean();
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    Set<Formula> formulas = new HashSet<>();

    if (this.isAtomic()) {
      formulas.add(this);
    }

    if (this.leftFormula.isBoolean()) {
      formulas.addAll(this.leftFormula.extractPredicateFormulas());
    }

    if (this.rightFormula.isBoolean()) {
      formulas.addAll(this.rightFormula.extractPredicateFormulas());
    }

    return formulas;
  }
}
