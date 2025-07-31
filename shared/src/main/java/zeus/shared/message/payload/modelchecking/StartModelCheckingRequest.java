package zeus.shared.message.payload.modelchecking;

import java.util.Map;
import java.util.UUID;

public class StartModelCheckingRequest {
  private Path path;
  private Map<UUID, PredicateValuation> predicateValuations;

  public StartModelCheckingRequest(Path path) {
    this.path = path;
  }

  public StartModelCheckingRequest(Path path, Map<UUID, PredicateValuation> predicateValuations) {
    this(path);
    this.predicateValuations = predicateValuations;
  }

  public Path getPath() {
    return path;
  }

  public Map<UUID, PredicateValuation> getPredicateValuations() {
    return predicateValuations;
  }
}
