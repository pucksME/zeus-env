package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class BooleanLiteralFormula extends LiteralFormula<Boolean> {
  public BooleanLiteralFormula(boolean value) {
    super(value);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkBool(this.value);
  }
}
