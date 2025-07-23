package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class IntegerVariableFormula extends VariableFormula {
  public IntegerVariableFormula(String id) {
    super(id);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkIntConst(this.id);
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new IntegerVariableFormula(this.id);
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
