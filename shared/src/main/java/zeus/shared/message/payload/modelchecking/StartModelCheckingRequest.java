package zeus.shared.message.payload.modelchecking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class StartModelCheckingRequest {
  private UUID verificationUuid;
  private Path path;
  private Map<UUID, PredicateValuation> predicateValuations;

  public StartModelCheckingRequest(UUID verificationUuid, Path path) {
    this.verificationUuid = verificationUuid;
    this.path = path;
  }

  public StartModelCheckingRequest(
    UUID verificationUuid,
    Path path,
    Map<UUID, PredicateValuation> predicateValuations
  ) {
    this(verificationUuid, path);
    this.predicateValuations = predicateValuations;
  }

  public Path getPath() {
    return path;
  }

  public Optional<Map<UUID, PredicateValuation>> getPredicateValuations() {
    return Optional.ofNullable(predicateValuations);
  }

  public UUID getVerificationUuid() {
    return verificationUuid;
  }
}
