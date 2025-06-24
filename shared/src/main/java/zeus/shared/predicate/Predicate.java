package zeus.shared.predicate;

import zeus.shared.formula.Formula;

import java.util.UUID;

public abstract class Predicate {
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

  public Formula getFormula() {
    return formula;
  }
}
