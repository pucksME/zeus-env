package zeus.shared.formula;

import java.util.HashSet;
import java.util.Set;

public abstract class LiteralFormula<T> extends Formula {
  T value;

  public LiteralFormula(T value) {
    this.value = value;
  }

  @Override
  public Set<String> getReferencedVariables() {
    return Set.of();
  }

  @Override
  public boolean containsVariables() {
    return false;
  }

  @Override
  public Set<Formula> extractPredicateFormulas() {
    return new HashSet<>(Set.of(this));
  }

  @Override
  public String toString() {
    return String.valueOf(this.value);
  }
}
