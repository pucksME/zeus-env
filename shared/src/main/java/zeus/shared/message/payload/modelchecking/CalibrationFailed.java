package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record CalibrationFailed(UUID uuid, Path path) {
}
