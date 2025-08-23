package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class EqualFormula extends BinaryEqualityFormula {
  public EqualFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkEq(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new EqualFormula(this.leftFormula.replace(variable, formula), this.rightFormula.replace(variable, formula));
  }

  @Override
  public String toString() {
    return String.format("%s = %s", this.leftFormula, this.rightFormula);
  }
}
