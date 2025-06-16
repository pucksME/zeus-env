package zeus.shared.message.payload.abstraction;

import zeus.shared.formula.Formula;
import zeus.shared.predicate.Predicate;

import java.util.HashMap;
import java.util.UUID;

public record AbstractRequest(
  UUID uuid,
  HashMap<UUID, Predicate> predicates,
  HashMap<UUID, Boolean> predicateValuations,
  Formula expression
) {
}
