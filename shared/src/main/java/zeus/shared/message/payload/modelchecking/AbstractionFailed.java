package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record AbstractionFailed(UUID nodeUuid, String message) {
}
