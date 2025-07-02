package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class NotEqualFormula extends BinaryFormula {
  public NotEqualFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkNot(context.mkEq(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context)));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new NotEqualFormula(this.leftFormula.replace(variable, formula), this.rightFormula.replace(variable, formula));
  }
}
