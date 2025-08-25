package zeus.shared.message.payload.storage;

import java.util.UUID;

public record AddVisitedComponentResponse(UUID requestUuid, boolean existed) {
}
