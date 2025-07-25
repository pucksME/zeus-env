package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class StringVariableFormula extends VariableFormula {
  public StringVariableFormula(String id) {
    super(id);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkConst(this.id, context.mkStringSort());
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
