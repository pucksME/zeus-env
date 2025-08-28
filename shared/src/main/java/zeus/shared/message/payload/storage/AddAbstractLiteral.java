package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.abstraction.AbstractLiteral;
import zeus.shared.message.payload.modelchecking.Valuation;

import java.util.Set;
import java.util.UUID;

public record AddAbstractLiteral(UUID verificationUuid, Set<Valuation> valuations, AbstractLiteral abstractLiteral) {
}
