package zeus.zeusverifier.node.abstraction;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import zeus.shared.formula.Formula;
import zeus.shared.formula.unary.NotFormula;
import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractLiteral;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.message.payload.storage.AddAbstractLiteral;
import zeus.shared.predicate.Predicate;

import java.util.*;
import java.util.stream.Stream;

public class Abstractor {
  private final UUID verificationUuid;
  private final AbstractionNode abstractionNode;

  public Abstractor(UUID verificationUuid, AbstractionNode abstractionNode) {
    this.verificationUuid = verificationUuid;
    this.abstractionNode = abstractionNode;
  }

  public AbstractionResult computeAbstraction(
    Map<UUID, Predicate> predicates,
    Map<UUID, PredicateValuation> predicateValuations,
    Formula expression,
    ExpressionIdentifier expressionIdentifier
  ) {
    try (Context context = new Context()) {
      Solver solver = context.mkSolver();
      List<Formula> formulas = new ArrayList<>();

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
          ? uuidPredicate.getValue().getFormula()
          : new NotFormula(uuidPredicate.getValue().getFormula()));
      }

      Set<Integer> unsatisfiableCore = Formula.getUnsatisfiableCore(
        Stream.concat(formulas.stream(), Stream.of(new NotFormula(expression))).toList(),
        context,
        solver
      );

      if (!unsatisfiableCore.isEmpty()) {
        AbstractLiteral abstractLiteral = AbstractLiteral.TRUE;

        this.abstractionNode.sendMessage(new Message<>(new AddAbstractLiteral(
          this.verificationUuid,
          Valuation.filter(Stream.concat(
            predicateValuations.values().stream(),
            Stream.of(new ExpressionValuation(false, expressionIdentifier))
          ).toList(), unsatisfiableCore),
          abstractLiteral
        ), new Recipient(NodeType.STORAGE, NodeSelection.ANY)));

        return new AbstractionResult(abstractLiteral);
      }

      unsatisfiableCore = Formula.getUnsatisfiableCore(
        Stream.concat(formulas.stream(), Stream.of(expression)).toList(),
        context,
        solver
      );

      if (!unsatisfiableCore.isEmpty()) {
        AbstractLiteral abstractLiteral = AbstractLiteral.FALSE;

        this.abstractionNode.sendMessage(new Message<>(new AddAbstractLiteral(
          this.verificationUuid,
          Valuation.filter(Stream.concat(
            predicateValuations.values().stream(),
            Stream.of(new ExpressionValuation(true, expressionIdentifier))
          ).toList(), unsatisfiableCore),
          abstractLiteral
        ), new Recipient(NodeType.STORAGE, NodeSelection.ANY)));

        return new AbstractionResult(abstractLiteral);
      }
    }

    return new AbstractionResult(AbstractLiteral.NON_DETERMINISTIC);
  }
}
