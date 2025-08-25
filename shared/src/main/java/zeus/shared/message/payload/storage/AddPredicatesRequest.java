package zeus.shared.message.payload.storage;

import zeus.shared.formula.Formula;

import java.util.Set;
import java.util.UUID;

public record AddPredicatesRequest(
  UUID uuid,
  UUID verificationUuid,
  UUID counterexampleAnalysisNodeUuid,
  Set<Formula> formulas
) {
}
