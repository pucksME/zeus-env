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

  public Formula getFormula() {
    return formula;
  }
}
