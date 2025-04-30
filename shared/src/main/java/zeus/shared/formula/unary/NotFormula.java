package zeus.shared.formula.unary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

public class NotFormula extends UnaryFormula {
  public NotFormula(Formula formula) {
    super(formula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkNot(this.formula.toFormula(context));
  }
}
