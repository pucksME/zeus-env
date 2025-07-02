package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class FloatVariableFormula extends VariableFormula {
  public FloatVariableFormula(String id) {
    super(id);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkRealConst(this.id);
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new FloatVariableFormula(this.id);
  }
}
