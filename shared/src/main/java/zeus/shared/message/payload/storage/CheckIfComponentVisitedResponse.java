package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.modelchecking.Location;

import java.util.UUID;

public record CheckIfComponentVisitedResponse(UUID requestUuid, Location location, boolean visited) {
}
