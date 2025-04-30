package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class DivideFormula extends BinaryFormula {
  public DivideFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkDiv(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }
}
