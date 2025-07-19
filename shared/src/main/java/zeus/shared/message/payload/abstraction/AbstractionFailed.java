package zeus.shared.message.payload.abstraction;

import java.util.UUID;

public record AbstractionFailed(UUID nodeUuid, String message) {
}
