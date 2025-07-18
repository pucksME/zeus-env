package zeus.shared.message.payload.counterexampleanalysis;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.UUID;

public record AnalyzeCounterExampleResponse(UUID uuid, Path path) {
}
