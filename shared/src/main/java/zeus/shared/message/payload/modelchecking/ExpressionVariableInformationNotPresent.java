package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record ExpressionVariableInformationNotPresent(UUID nodeUuid, int line, int linePosition) {
}
