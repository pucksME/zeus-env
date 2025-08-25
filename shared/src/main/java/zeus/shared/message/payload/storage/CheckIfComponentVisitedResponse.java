package zeus.shared.message.payload.storage;

import zeus.shared.message.payload.modelchecking.Location;

import java.util.UUID;

public record CheckIfComponentVisitedResponse(UUID uuid, Location location, boolean visited) {
}
