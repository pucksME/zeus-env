package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.List;
import java.util.Optional;


public class CompareBinaryFormula extends BinaryFormula {
  CompareBinaryFormulaType compareBinaryFormulaType;

  public CompareBinaryFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    CompareBinaryFormulaType compareBinaryFormulaType
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.compareBinaryFormulaType = compareBinaryFormulaType;
  }

  @Override
  public void check() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<Type> leftFormulaTypeOptional = this.leftFormula.evaluateType();

    if (leftFormulaTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> rightFormulaTypeOptional = this.rightFormula.evaluateType();

    if (rightFormulaTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    if (!leftFormulaTypeOptional.get().compatible(rightFormulaTypeOptional.get())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(new PrimitiveType(PrimitiveTypeType.BOOLEAN));
  }

  private boolean hasDirectTranslation() {
    if (this.compareBinaryFormulaType == CompareBinaryFormulaType.EQUAL ||
      this.compareBinaryFormulaType == CompareBinaryFormulaType.NOT_EQUAL) {
      Optional<Type> typeOptional = this.leftFormula.evaluateType();
      if (typeOptional.isEmpty()) {
        return false;
      }

      Type type = typeOptional.get();
      return !(type instanceof PrimitiveType && ((PrimitiveType) type).getType() == PrimitiveTypeType.BOOLEAN);
    }

    return true;
  }

  @Override
  public String translate() {
    Optional<Type> typeOptional = this.leftFormula.evaluateType();

    if (typeOptional.isEmpty()) {
      throw new RuntimeException("Could not directly translate compare binary formula: type not present");
    }

    Type type = typeOptional.get();

    if (type instanceof PrimitiveType && ((PrimitiveType) type).getType() == PrimitiveTypeType.STRING) {
      return String.format(
        "%s.equals(%s)",
        this.leftFormula.translate(),
        this.rightFormula.translate()
      );
    }

    return String.format(
      "%s %s %s",
      this.leftFormula.translate(),
      this.translateOperator(),
      this.rightFormula.translate()
    );
  }

  private String translateOperator() {
    return switch (this.compareBinaryFormulaType) {
      case EQUAL -> "==";
      case NOT_EQUAL -> "!=";
      case GREATER_THAN -> ">";
      case LESS_THAN -> "<";
      case GREATER_EQUAL_THAN -> ">=";
      case LESS_EQUAL_THAN -> "<=";
    };
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    if (this.hasDirectTranslation()) {
      return this.translate();
    }

    return String.format(
      "pre[%s] %s pre[%s]",
      subFormulas.indexOf(this.getLeftFormula()),
      this.translateOperator(),
      subFormulas.indexOf(this.getRightFormula())
    );
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    if (this.hasDirectTranslation()) {
      return this.translate();
    }

    return String.format(
      "now[%s] %s now[%s]",
      subFormulas.indexOf(this.getLeftFormula()),
      this.translateOperator(),
      subFormulas.indexOf(this.getRightFormula())
    );
  }
}
