package zeus.shared.message.payload.abstraction;

import zeus.shared.formula.Formula;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.predicate.Predicate;

import java.util.Map;
import java.util.UUID;

public record AbstractRequest(
  UUID uuid,
  UUID verificationUuid,
  UUID modelCheckingNodeUuid,
  Map<UUID, Predicate> predicates,
  Map<UUID, PredicateValuation> predicateValuations,
  Formula expression
) {
}
