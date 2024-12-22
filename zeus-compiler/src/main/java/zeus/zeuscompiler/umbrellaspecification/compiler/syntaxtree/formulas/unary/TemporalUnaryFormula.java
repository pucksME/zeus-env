package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

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

  @Override
  public String translate() {
    throw new RuntimeException("Could not directly translate temporal unary formula");
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return String.format("pre[%s]", subFormulas.indexOf(this.getFormula()));
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return switch (this.temporalUnaryFormulaType) {
      case YESTERDAY-> String.format("pre[%s]", subFormulas.indexOf(this.formula));
      case ONCE -> String.format("pre[%s] || now[%s]", subFormulas.indexOf(this), subFormulas.indexOf(this.formula));
      case HISTORICALLY -> String.format(
        "pre[%s] && now[%s]",
        subFormulas.indexOf(this),
        subFormulas.indexOf(this.formula)
      );
    };
  }
}
