package zeus.shared.predicate;

import zeus.shared.formula.Formula;

import java.util.UUID;

public class Predicate {
  UUID uuid;
  Formula formula;

  public Predicate(UUID uuid, Formula formula) {
    this.uuid = uuid;
    this.formula = formula;
  }

  @Override
  public int hashCode() {
    return this.uuid.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof Predicate && uuid.equals(((Predicate) obj).uuid);
  }

  public UUID getUuid() {
    return uuid;
  }

  public Formula getFormula() {
    return formula;
  }

  @Override
  public String toString() {
    return String.format("p_%s: %s", this.uuid.toString().substring(0, 3), this.formula);
  }
}
