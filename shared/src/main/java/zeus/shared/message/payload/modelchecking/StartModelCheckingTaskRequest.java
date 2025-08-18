package zeus.shared.message.payload.modelchecking;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class StartModelCheckingTaskRequest {
  private final UUID uuid;
  private final UUID verificationUuid;
  private final Path path;
  private Map<UUID, PredicateValuation> predicateValuations;

  public StartModelCheckingTaskRequest(UUID verificationUuid, Path path) {
    this.uuid = UUID.randomUUID();
    this.verificationUuid = verificationUuid;
    this.path = path;
  }

  public StartModelCheckingTaskRequest(
    UUID verificationUuid,
    Path path,
    Map<UUID, PredicateValuation> predicateValuations
  ) {
    this(verificationUuid, path);
    this.predicateValuations = predicateValuations;
  }

  public UUID getUuid() {
    return uuid;
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
