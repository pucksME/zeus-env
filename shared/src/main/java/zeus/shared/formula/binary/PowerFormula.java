package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class PowerFormula extends BinaryArithmeticFormula {
  public PowerFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkPower(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new PowerFormula(this.leftFormula.replace(variable, formula), this.rightFormula.replace(variable, formula));
  }
}
