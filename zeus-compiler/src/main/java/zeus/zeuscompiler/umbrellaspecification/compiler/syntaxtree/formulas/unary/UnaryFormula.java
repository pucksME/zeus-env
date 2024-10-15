package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public abstract class UnaryFormula extends Formula {
  Formula formula;

  public UnaryFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition);
    this.formula = formula;
  }

  @Override
  public List<Formula> getSubFormulas() {
    Optional<Type> formulaTypeOptional = this.formula.evaluateType();

    if (formulaTypeOptional.isEmpty()) {
      throw new RuntimeException("Could not get sub formulas of unary formula: type not present");
    }

    Type formulaType = formulaTypeOptional.get();

    if (formulaType instanceof PrimitiveType &&
      ((PrimitiveType) formulaType).getType() != PrimitiveTypeType.BOOLEAN) {
      return new ArrayList<>(List.of(this));
    }

    return Stream.concat(
      Stream.of(this),
      this.formula.getSubFormulas().stream()
    ).toList();
  }
}
