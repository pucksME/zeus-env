package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;

public abstract class UnaryFormula extends Formula {
  Formula formula;

  public UnaryFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition);
    this.formula = formula;
  }
}
