package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class LiteralFormula extends Formula {
  String value;
  LiteralFormulaType literalFormulaType;

  public LiteralFormula(int line, int linePosition, String value, LiteralFormulaType literalFormulaType) {
    super(line, linePosition);
    this.literalFormulaType = literalFormulaType;
    this.value = (this.literalFormulaType == LiteralFormulaType.STRING)
      ? value.substring(1, value.length() - 1)
      : value;
  }

  public String getValue() {
    return value;
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
      return new ArrayList<>();
    }

    return new ArrayList<>(List.of(this));
  }

  @Override
  public boolean accessesResponse() {
    return false;
  }

  @Override
  public String translate() {
    return (this.literalFormulaType == LiteralFormulaType.STRING)
      ? String.format("\"%s\"", this.getValue())
      : this.getValue();
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return this.translate();
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return this.translatePre(subFormulas);
  }
}
