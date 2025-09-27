package zeus.shared.message.payload.counterexampleanalysis;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.UUID;

public record InvalidCounterexample(UUID verificationUuid, UUID modelCheckingTaskUuid, Path path, Path pivotPath) {
}
