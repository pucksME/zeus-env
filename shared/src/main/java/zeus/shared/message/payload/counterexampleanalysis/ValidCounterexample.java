package zeus.shared.message.payload.counterexampleanalysis;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.Set;
import java.util.UUID;

public record ValidCounterexample(
  UUID verificationUuid,
  UUID modelCheckingTaskUuid,
  Path path,
  Set<VariableAssignment> variableAssignments
) {
}
