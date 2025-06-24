package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record UnsupportedComponent(UUID nodeUuid, String componentName) {
}
