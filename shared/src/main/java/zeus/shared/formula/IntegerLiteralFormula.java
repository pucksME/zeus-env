package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class IntegerLiteralFormula extends LiteralFormula<Integer> {
  public IntegerLiteralFormula(int value) {
    super(value);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkInt(this.value);
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new IntegerLiteralFormula(this.value);
  }
}
