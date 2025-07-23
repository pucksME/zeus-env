package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class FloatLiteralFormula extends LiteralFormula<Float> {
  public FloatLiteralFormula(float value) {
    super(value);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkReal(this.value.toString());
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new FloatLiteralFormula(this.value);
  }

  @Override
  public boolean isBoolean() {
    return false;
  }

  @Override
  public boolean isAtomic() {
    return true;
  }
}
