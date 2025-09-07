package zeus.shared.message.payload.modelchecking;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DistributeModelCheckingRequest {
  private final UUID uuid;
  private final UUID verificationUuid;
  private final Path path;
  private final List<Map<UUID, PredicateValuation>> predicateValuations;
  private UUID invalidCounterexampleModelCheckingTaskUuid;

  public DistributeModelCheckingRequest(
    UUID uuid,
    UUID verificationUuid,
    Path path,
    List<Map<UUID, PredicateValuation>> predicateValuations
  ) {
    this.uuid = uuid;
    this.verificationUuid = verificationUuid;
    this.path = path;
    this.predicateValuations = predicateValuations;
    this.invalidCounterexampleModelCheckingTaskUuid = null;
  }

  public DistributeModelCheckingRequest(
    UUID uuid,
    UUID verificationUuid,
    Path path,
    List<Map<UUID, PredicateValuation>> predicateValuations,
    UUID invalidCounterexampleModelCheckingTaskUuid
  ) {
    this(uuid, verificationUuid, path, predicateValuations);
    this.invalidCounterexampleModelCheckingTaskUuid = invalidCounterexampleModelCheckingTaskUuid;
  }

  public List<StartModelCheckingTaskRequest> getStartModelCheckingRequests() {
    return this.predicateValuations.stream()
      .map(predicateValuations -> new StartModelCheckingTaskRequest(
        this.verificationUuid,
        this.path,
        predicateValuations
      ))
      .toList();
  }

  public Path getPath() {
    return path;
  }

  public List<Map<UUID, PredicateValuation>> getPredicateValuations() {
    return predicateValuations;
  }

  public UUID getUuid() {
    return uuid;
  }

  public UUID getVerificationUuid() {
    return verificationUuid;
  }

  public Optional<UUID> getInvalidCounterexampleModelCheckingTaskUuid() {
    return Optional.ofNullable(invalidCounterexampleModelCheckingTaskUuid);
  }
}
