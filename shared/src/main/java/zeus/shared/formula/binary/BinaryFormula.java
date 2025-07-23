package zeus.shared.formula.binary;

import zeus.shared.formula.Formula;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class BinaryFormula extends Formula {
  Formula leftFormula;
  Formula rightFormula;

  public BinaryFormula(Formula leftFormula, Formula rightFormula) {
    this.leftFormula = leftFormula;
    this.rightFormula = rightFormula;
  }

  @Override
  public Set<String> getReferencedVariables() {
    return Stream.concat(
      this.leftFormula.getReferencedVariables().stream(),
      this.rightFormula.getReferencedVariables().stream()
    ).collect(Collectors.toSet());
  }

  @Override
  public boolean containsVariables() {
    return this.leftFormula.containsVariables() || this.rightFormula.containsVariables();
  }
}
