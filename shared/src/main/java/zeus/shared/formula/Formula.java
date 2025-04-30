package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public abstract class Formula {
  public abstract Expr toFormula(Context context);
}
