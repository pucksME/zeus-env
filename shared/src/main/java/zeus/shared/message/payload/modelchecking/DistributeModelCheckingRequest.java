package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record DistributeModelCheckingRequest(
  Path path,
  Map<UUID, Predicate> predicates,
  List<Map<UUID, PredicateValuation>> predicateValuations
) {
}
