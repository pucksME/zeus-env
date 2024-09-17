package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

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
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }
}
