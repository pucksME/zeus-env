package zeus.shared.message.payload.storage;

import java.util.Optional;
import java.util.UUID;

public class CheckPredicateValuationsResponse {
  private final UUID requestUuid;
  private Boolean abstractValue;

  public CheckPredicateValuationsResponse(UUID requestUuid) {
    this.requestUuid = requestUuid;
    this.abstractValue = null;
  }

  public CheckPredicateValuationsResponse(UUID requestUuid, Boolean abstractValue) {
    this(requestUuid);
    this.abstractValue = abstractValue;
  }

  public UUID getRequestUuid() {
    return requestUuid;
  }

  public Optional<Boolean> getAbstractValue() {
    return Optional.ofNullable(abstractValue);
  }
}
