package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;

import java.util.List;
import java.util.Optional;

public class TemporalBinaryFormula extends BinaryFormula {
  TemporalBinaryFormulaType temporalBinaryFormulaType;

  public TemporalBinaryFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    TemporalBinaryFormulaType temporalBinaryFormulaType
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.temporalBinaryFormulaType = temporalBinaryFormulaType;
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

    Type leftFormulaType = leftFormulaTypeOptional.get();

    if (!(leftFormulaType instanceof PrimitiveType) ||
      ((PrimitiveType) leftFormulaType).getType() != PrimitiveTypeType.BOOLEAN) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    Optional<Type> rightFormulaTypeOptional = this.rightFormula.evaluateType();

    if (rightFormulaTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    if (!leftFormulaType.compatible(rightFormulaTypeOptional.get())) {
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

  @Override
  public boolean isTemporal() {
    return true;
  }

  @Override
  public String translate() {
    throw new RuntimeException("Could not directly translate temporal binary formula");
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return switch (this.temporalBinaryFormulaType) {
      case SINCE -> String.format("pre[%s]", subFormulas.indexOf(this.rightFormula));
    };
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return switch (this.temporalBinaryFormulaType) {
      case SINCE -> String.format(
        "pre[%s] || (pre[%s] && now[%s])",
        subFormulas.indexOf(this),
        subFormulas.indexOf(this.rightFormula),
        subFormulas.indexOf(this.leftFormula)
      );
    };
  }
}
