package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.PredicateValuation;

import java.util.Set;
import java.util.UUID;

public record AddVisitedComponent(
  UUID verificationUuid,
  Location location,
  Set<PredicateValuation> predicateValuations
) {
}
