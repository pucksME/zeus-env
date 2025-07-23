package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class StringLiteralFormula extends LiteralFormula<String> {
  public StringLiteralFormula(String value) {
    super(value);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkString(this.value);
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new StringLiteralFormula(this.value);
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
