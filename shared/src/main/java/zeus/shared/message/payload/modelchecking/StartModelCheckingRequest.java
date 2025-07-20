package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.Map;
import java.util.UUID;

public record StartModelCheckingRequest(
  Path path,
  Map<UUID, Predicate> predicates,
  Map<UUID, PredicateValuation> predicateValuations
) {
}
