package zeus.shared.formula;

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
}
