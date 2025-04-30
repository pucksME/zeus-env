package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class LessEqualFormula extends BinaryFormula {
  public LessEqualFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkLe(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }
}
