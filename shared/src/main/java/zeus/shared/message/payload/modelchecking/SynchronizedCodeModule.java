package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record SynchronizedCodeModule(UUID nodeUuid, UUID verificationUuid) {
}
