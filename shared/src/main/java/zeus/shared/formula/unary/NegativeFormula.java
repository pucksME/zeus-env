package zeus.shared.formula.unary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

import java.util.HashSet;
import java.util.Set;

public class NegativeFormula extends UnaryArithmeticFormula {
  public NegativeFormula(Formula formula) {
    super(formula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkMul(this.formula.toFormula(context), context.mkInt(-1));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new NegativeFormula(this.formula.replace(variable, formula));
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    return new HashSet<>(Set.of(this));
  }
}
