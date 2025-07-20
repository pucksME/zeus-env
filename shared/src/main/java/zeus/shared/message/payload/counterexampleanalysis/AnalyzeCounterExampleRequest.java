package zeus.shared.message.payload.counterexampleanalysis;

import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.predicate.Predicate;

import java.util.Map;
import java.util.UUID;

public record AnalyzeCounterExampleRequest(UUID uuid, Path path, Map<UUID, Predicate> predicates) {
}
