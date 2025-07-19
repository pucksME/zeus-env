package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record ModelCheckingFailed(UUID nodeUuid, String message) {
}
