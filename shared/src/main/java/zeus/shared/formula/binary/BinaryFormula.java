package zeus.shared.formula.binary;

import zeus.shared.formula.Formula;

public abstract class BinaryFormula extends Formula {
  Formula leftFormula;
  Formula rightFormula;

  public BinaryFormula(Formula leftFormula, Formula rightFormula) {
    this.leftFormula = leftFormula;
    this.rightFormula = rightFormula;
  }
}
