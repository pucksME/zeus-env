package zeus.shared.message.payload.abstraction;

import zeus.shared.formula.Formula;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.predicate.Predicate;

import java.util.HashMap;
import java.util.UUID;

public record AbstractRequest(
  UUID uuid,
  HashMap<UUID, Predicate> predicates,
  HashMap<UUID, PredicateValuation> predicateValuations,
  Formula expression
) {
}
