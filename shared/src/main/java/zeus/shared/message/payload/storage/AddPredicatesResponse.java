package zeus.shared.message.payload.storage;

import zeus.shared.predicate.Predicate;

import java.util.Set;
import java.util.UUID;

public record AddPredicatesResponse(UUID requestUuid, Set<Predicate> predicates) {
}
