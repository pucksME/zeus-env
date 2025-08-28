package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.modelchecking.PredicateValuation;

import java.util.Set;
import java.util.UUID;

public record GetAbstractLiteralRequest(
  UUID uuid,
  UUID verificationUuid,
  UUID abstractionNodeUuid,
  Set<PredicateValuation> predicateValuations
) {
}
