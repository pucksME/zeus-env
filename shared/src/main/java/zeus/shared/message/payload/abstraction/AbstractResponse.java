package zeus.shared.message.payload.abstraction;

import java.util.UUID;

public record AbstractResponse(UUID uuid, AbstractLiteral abstractLiteral) {
}
