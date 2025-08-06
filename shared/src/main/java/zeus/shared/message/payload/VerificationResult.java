package zeus.shared.message.payload;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.Optional;
import java.util.UUID;

public class VerificationResult {
  private UUID verificationUuid;
  private Path validCounterexample;

  public VerificationResult(UUID verificationUuid) {
    this.verificationUuid = verificationUuid;
  }

  public VerificationResult(UUID verificationUuid, Path validCounterexample) {
    this(verificationUuid);
    this.validCounterexample = validCounterexample;
  }

  public Optional<Path> getValidCounterexample() {
    return Optional.ofNullable(this.validCounterexample);
  }

  public UUID getVerificationUuid() {
    return verificationUuid;
  }
}
