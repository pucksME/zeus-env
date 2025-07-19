package zeus.shared.message.payload.counterexampleanalysis;

import java.util.UUID;

public record CounterexampleAnalysisFailed(UUID nodeUuid, String message) {
}
