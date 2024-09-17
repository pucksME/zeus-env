package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;

import java.util.Optional;

public class TemporalUnaryFormula extends UnaryFormula {
  TemporalUnaryFormulaType temporalUnaryFormulaType;

  public TemporalUnaryFormula(
    int line,
    int linePosition,
    Formula formula,
    TemporalUnaryFormulaType temporalUnaryFormulaType
  ) {
    super(line, linePosition, formula);
    this.temporalUnaryFormulaType = temporalUnaryFormulaType;
  }

  @Override
  public void check() {
    this.evaluateType();
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<Type> formulaTypeOptional = this.formula.evaluateType();

    if (formulaTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type formulaType = formulaTypeOptional.get();

    if (!(formulaType instanceof PrimitiveType) ||
      ((PrimitiveType) formulaType).getType() != PrimitiveTypeType.BOOLEAN) {
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
}
