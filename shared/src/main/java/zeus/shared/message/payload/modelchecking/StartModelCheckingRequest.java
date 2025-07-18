package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.Set;

public record StartModelCheckingRequest(
  Path path,
  Set<Predicate> predicates,
  Set<PredicateValuation> predicateValuations
) {
}
