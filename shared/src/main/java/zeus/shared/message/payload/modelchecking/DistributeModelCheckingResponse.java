package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record DistributeModelCheckingResponse(
  UUID requestUuid,
  UUID verificationUuid,
  UUID invalidCounterexampleModelCheckingTaskUuid
) {
}
