package zeus.shared.message.payload.modelchecking;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DistributeModelCheckingRequest {
  private UUID verificationUuid;
  private Path path;
  private List<Map<UUID, PredicateValuation>> predicateValuations;

  public DistributeModelCheckingRequest(UUID verificationUuid, Path path, List<Map<UUID, PredicateValuation>> predicateValuations) {
    this.verificationUuid = verificationUuid;
    this.path = path;
    this.predicateValuations = predicateValuations;
  }

  public List<StartModelCheckingRequest> getStartModelCheckingRequests() {
    return this.predicateValuations.stream()
      .map(predicateValuations -> new StartModelCheckingRequest(
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
}
