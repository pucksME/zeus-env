package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public class BooleanVariableFormula extends VariableFormula {
  public BooleanVariableFormula(String id) {
    super(id);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkBoolConst(this.id);
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new BooleanVariableFormula(this.id);
  }
}
