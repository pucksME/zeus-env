package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class LessFormula extends BinaryFormula {
  public LessFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkLt(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }
}
