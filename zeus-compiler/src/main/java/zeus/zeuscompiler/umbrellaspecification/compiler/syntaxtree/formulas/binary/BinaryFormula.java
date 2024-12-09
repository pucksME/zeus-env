package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class BinaryFormula extends Formula {
  Formula leftFormula;
  Formula rightFormula;

  public BinaryFormula(int line, int linePosition, Formula leftFormula, Formula rightFormula) {
    super(line, linePosition);
    this.leftFormula = leftFormula;
    this.rightFormula = rightFormula;
  }

  public Formula getLeftFormula() {
    return leftFormula;
  }

  public Formula getRightFormula() {
    return rightFormula;
  }

  @Override
  public List<Formula> getSubFormulas() {
    Optional<Type> leftFormulaTypeOptional = this.leftFormula.evaluateType();
    Optional<Type> rightFormulaTypeOptional = this.rightFormula.evaluateType();

    if (leftFormulaTypeOptional.isEmpty() || rightFormulaTypeOptional.isEmpty()) {
      throw new RuntimeException("Could not get sub formulas of binary formula: types not present");
    }

    Type leftFormulaType = leftFormulaTypeOptional.get();
    Type rightFormulaType = rightFormulaTypeOptional.get();

    if (!leftFormulaType.compatible(rightFormulaType)) {
      throw new RuntimeException("Could not get sub formulas of binary formula: types are not compatible");
    }

    if (leftFormulaType instanceof PrimitiveType &&
      ((PrimitiveType) leftFormulaType).getType() != PrimitiveTypeType.BOOLEAN) {
      return new ArrayList<>(List.of(this));
    }

    return Stream.concat(Stream.of(this), Stream.concat(
      this.leftFormula.getSubFormulas().stream(),
      this.rightFormula.getSubFormulas().stream()
    )).toList();
  }
}
