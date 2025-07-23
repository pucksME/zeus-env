package zeus.shared.formula.binary;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.shared.formula.Formula;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AndFormula extends BinaryBooleanFormula {
  public AndFormula(Formula leftFormula, Formula rightFormula) {
    super(leftFormula, rightFormula);
  }

  @Override
  public Expr toFormula(Context context) {
    return context.mkAnd(this.leftFormula.toFormula(context), this.rightFormula.toFormula(context));
  }

  @Override
  public Formula replace(String variable, Formula formula) {
    return new AndFormula(this.leftFormula.replace(variable, formula), this.rightFormula.replace(variable, formula));
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    return Stream.concat(
      this.leftFormula.extractPredicateFormulas().stream(),
      this.rightFormula.extractPredicateFormulas().stream()
    ).collect(Collectors.toSet());
  }
}
