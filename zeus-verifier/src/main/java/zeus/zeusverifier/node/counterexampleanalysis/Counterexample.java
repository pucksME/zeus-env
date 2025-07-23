package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.payload.modelchecking.Path;

public record Counterexample(Path path, boolean valid) {
}
