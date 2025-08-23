package zeus.shared.formula.unary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

import java.util.HashSet;
import java.util.Set;

public class NotFormula extends UnaryBooleanFormula {
  public NotFormula(Formula formula) {
    super(formula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkNot(this.formula.toFormula(context));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new NotFormula(this.formula.replace(variable, formula));
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    return new HashSet<>(Set.of(this.formula));
  }

  @Override
  public String toString() {
    return String.format("!%s", this.formula);
  }
}
