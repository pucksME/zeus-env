package zeus.zeusverifier.node.abstraction;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import zeus.shared.formula.Formula;
import zeus.shared.message.payload.abstraction.AbstractLiteral;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.predicate.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class Abstractor {
  private boolean check(List<Expr> formulas, Solver solver, Context context) {
    return solver.check(context.mkAnd(formulas.toArray(Expr[]::new))) == Status.UNSATISFIABLE;
  }

  private boolean check(List<Expr> formulars, Expr expression, Solver solver, Context context) {
    return this.check(Stream.concat(formulars.stream(), Stream.of(expression)).toList(), solver, context);
  }

  public AbstractionResult computeAbstraction(
    Map<UUID, Predicate> predicates,
    Map<UUID, PredicateValuation> predicateValuations,
    Formula expression
  ) {
    try (Context context = new Context()) {
      Solver solver = context.mkSolver();
      List<Expr> formulas = new ArrayList<>();

      for (Map.Entry<UUID, Predicate> uuidPredicate : predicates.entrySet()) {
        PredicateValuation predicateValuation = predicateValuations.get(uuidPredicate.getKey());

        if (predicateValuation == null) {
          System.out.printf(
            "Could not compute abstraction: missing valuation for predicate \"%s\"%n",
            uuidPredicate.getKey()
          );

          return new AbstractionResult(AbstractionResultStatus.MISSING_PREDICATE_VALUATIONS);
        }

        formulas.add((predicateValuation.getValue())
          ? uuidPredicate.getValue().getFormula().toFormula(context)
          : context.mkNot(uuidPredicate.getValue().getFormula().toFormula(context)));
      }

      Expr expressionFormula = expression.toFormula(context);

      if (this.check(formulas, context.mkNot(expressionFormula), solver, context)) {
        return new AbstractionResult(AbstractLiteral.TRUE);
      }

      if (this.check(formulas, expressionFormula, solver, context)) {
        return new AbstractionResult(AbstractLiteral.FALSE);
      }
    }

    return new AbstractionResult(AbstractLiteral.NON_DETERMINISTIC);
  }
}
