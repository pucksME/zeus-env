package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LiteralFormula extends Formula {
  LiteralFormulaType literalFormulaType;

  public LiteralFormula(int line, int linePosition, LiteralFormulaType literalFormulaType) {
    super(line, linePosition);
    this.literalFormulaType = literalFormulaType;
  }

  @Override
  public Optional<Type> evaluateType() {
    return switch (this.literalFormulaType) {
      case BOOLEAN -> Optional.of(new PrimitiveType(PrimitiveTypeType.BOOLEAN));
      case INT -> Optional.of(new PrimitiveType(PrimitiveTypeType.INT));
      case FLOAT -> Optional.of(new PrimitiveType(PrimitiveTypeType.FLOAT));
      case STRING -> Optional.of(new PrimitiveType(PrimitiveTypeType.STRING));
    };
  }

  @Override
  public void check() {
    this.evaluateType();
  }

  @Override
  public List<Formula> getSubFormulas() {
    if (this.literalFormulaType != LiteralFormulaType.BOOLEAN) {
      throw new RuntimeException("Could not get sub formulas of literal formula");
    }

    return new ArrayList<>(List.of(this));
  }
}
