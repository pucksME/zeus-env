package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import java.util.Set;

public abstract class Formula {
  final String className;

  public Formula() {
    this.className = this.getClass().getName();
  }

  public abstract Expr toFormula(Context context);

  public abstract Set<String> getReferencedVariables();

  public abstract Formula replace(String variable, Formula formula);

  public abstract boolean containsVariables();

  public abstract boolean isBoolean();

  public abstract boolean isAtomic();

  public abstract Set<Formula> extractPredicateFormulas();

  public boolean equals(Formula formula, Context context, Solver solver) {
    return solver.check(context.mkNot(context.mkEq(
      this.toFormula(context),
      formula.toFormula(context)
    ))) == Status.UNSATISFIABLE;
  }
}
