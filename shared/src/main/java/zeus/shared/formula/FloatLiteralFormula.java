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
}
