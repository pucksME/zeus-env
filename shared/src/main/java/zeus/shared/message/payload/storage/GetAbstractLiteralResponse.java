package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.abstraction.AbstractLiteral;

import java.util.Optional;
import java.util.UUID;

public class GetAbstractLiteralResponse {
  private final UUID requestUuid;
  private AbstractLiteral abstractLiteral;

  public GetAbstractLiteralResponse(UUID requestUuid) {
    this.requestUuid = requestUuid;
    this.abstractLiteral = null;
  }

  public GetAbstractLiteralResponse(UUID requestUuid, AbstractLiteral abstractLiteral) {
    this(requestUuid);
    this.abstractLiteral = abstractLiteral;
  }

  public UUID getRequestUuid() {
    return requestUuid;
  }

  public Optional<AbstractLiteral> getAbstractLiteral() {
    return Optional.ofNullable(abstractLiteral);
  }
}
