package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public record StopModelCheckingTaskRequest(
  UUID verificationUuid,
  UUID modelCheckingTaskUuid,
  StopModelCheckingTaskRequestStatus status
) {
}
