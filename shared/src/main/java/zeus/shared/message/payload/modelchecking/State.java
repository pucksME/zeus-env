package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class State {
  Location location;
  Set<Predicate> predicates;
  private boolean checked;

  public State(Location location) {
    this.location = location;
    this.predicates = null;
    this.checked = true;
  }

  public State(Location location, boolean checked) {
    this(location);
    this.checked = checked;
  }

  public State(Location location, Set<Predicate> predicates, boolean checked) {
    this(location, checked);
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

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  public boolean isChecked() {
    return checked;
  }

  @Override
  public String toString() {
    return String.format(
      "%s:%s {%s}",
      this.location.line(),
      this.location.linePosition(),
      (this.predicates != null)
        ? this.predicates.stream().map(Predicate::toString).collect(Collectors.joining(", "))
        : ""
    );
  }
}
