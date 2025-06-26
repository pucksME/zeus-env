package zeus.shared.formula;

import java.util.HashSet;
import java.util.Set;

public abstract class VariableFormula extends Formula {
  String id;

  public VariableFormula(String id) {
    this.id = id;
  }

  @Override
  public Set<String> getReferencedVariables() {
    return new HashSet<>(Set.of(this.id));
  }
}
