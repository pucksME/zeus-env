package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.Optional;
import java.util.Set;

public class State {
  Location location;
  Set<Predicate> predicates;

  public State(Location location) {
    this.location = location;
    this.predicates = null;
  }

  public State(Location location, Set<Predicate> predicates) {
    this(location);
    this.predicates = predicates;
  }

  public void setPredicates(Set<Predicate> predicates) {
    this.predicates = predicates;
  }

  public Optional<Set<Predicate>> getPredicates() {
    return Optional.ofNullable(predicates);
  }

  public Location getLocation() {
    return location;
  }
}
