package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

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


public class ArithmeticNegativeFormula extends UnaryFormula {
  public ArithmeticNegativeFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition, formula);
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
      (((PrimitiveType) formulaType).getType() != PrimitiveTypeType.INT &&
        ((PrimitiveType) formulaType).getType() != PrimitiveTypeType.FLOAT)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(new PrimitiveType(((PrimitiveType) formulaType).getType()));
  }

  @Override
  public String translate() {
    return String.format("%s%s", "-", this.formula.translate());
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return this.translate();
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return this.translate();
  }
}
