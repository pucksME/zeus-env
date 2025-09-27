package zeus.shared.message.payload;

import zeus.shared.message.payload.counterexampleanalysis.VariableAssignment;
import zeus.shared.message.payload.modelchecking.Path;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VerificationResult {
  private UUID verificationUuid;
  private Path validCounterexample;
  private Set<VariableAssignment> variableAssignments;

  public VerificationResult(UUID verificationUuid) {
    this.verificationUuid = verificationUuid;
  }

  public VerificationResult(UUID verificationUuid, Path validCounterexample, Set<VariableAssignment> variableAssignments) {
    this(verificationUuid);
    this.validCounterexample = validCounterexample;
    this.variableAssignments = variableAssignments;
  }

  public Optional<Path> getValidCounterexample() {
    return Optional.ofNullable(this.validCounterexample);
  }

  public Optional<Set<VariableAssignment>> getVariableAssignments() {
    return Optional.ofNullable(variableAssignments);
  }

  public UUID getVerificationUuid() {
    return verificationUuid;
  }
}
