package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

public abstract class Formula {
  final String className;

  public Formula() {
    this.className = this.getClass().getName();
  }

  public abstract Expr toFormula(Context context);
}
