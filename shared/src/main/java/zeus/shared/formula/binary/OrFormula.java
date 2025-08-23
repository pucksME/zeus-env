package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

import java.util.HashSet;
import java.util.Set;

public class OrFormula extends BinaryBooleanFormula {
  public OrFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkOr(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new OrFormula(this.leftFormula.replace(variable, formula), this.rightFormula.replace(variable, formula));
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    return new HashSet<>(Set.of(this));
  }

  @Override
  public String toString() {
    return String.format("%s | %s", this.leftFormula, this.rightFormula);
  }
}
