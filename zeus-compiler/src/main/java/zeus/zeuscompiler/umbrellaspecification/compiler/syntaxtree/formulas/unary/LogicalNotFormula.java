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


public class LogicalNotFormula extends UnaryFormula {
  public LogicalNotFormula(int line, int linePosition, Formula formula) {
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

    if (!(formulaType instanceof PrimitiveType)
      || ((PrimitiveType) formulaType).getType() != PrimitiveTypeType.BOOLEAN) {
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
    throw new RuntimeException("Could not directly translate logical not formula");
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return String.format("!pre[%s]", subFormulas.indexOf(this.getFormula()));
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return String.format("!now[%s]", subFormulas.indexOf(this.getFormula()));
  }
}
