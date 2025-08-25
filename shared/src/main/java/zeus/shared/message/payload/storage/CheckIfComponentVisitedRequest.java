package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.PredicateValuation;

import java.util.Set;
import java.util.UUID;

public record CheckIfComponentVisitedRequest(
  UUID uuid,
  UUID verificationUuid,
  UUID modelCheckingNodeUuid,
  Location location,
  Set<PredicateValuation> predicateValuations
) {
}
