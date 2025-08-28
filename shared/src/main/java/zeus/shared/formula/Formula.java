package zeus.shared.formula;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import java.util.*;
import java.util.stream.Collectors;

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

  public static Set<Integer> getUnsatisfiableCore(List<Formula> formulas, Context context, Solver solver) {
    for (int i = 0; i < formulas.size(); i++) {
      solver.assertAndTrack(formulas.get(i).toFormula(context), context.mkBoolConst(String.valueOf(i)));
    }

    if (solver.check() == Status.SATISFIABLE) {
      solver.reset();
      return new HashSet<>();
    }

    Set<Integer> unsatisfiableCore = Arrays.stream(solver.getUnsatCore())
      .map(expr -> Integer.parseInt(expr.getFuncDecl().getName().toString()))
      .collect(Collectors.toSet());

    solver.reset();
    return unsatisfiableCore;
  }
}
