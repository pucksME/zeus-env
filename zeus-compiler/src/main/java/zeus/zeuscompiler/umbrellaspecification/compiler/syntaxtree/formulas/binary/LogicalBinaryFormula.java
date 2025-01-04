package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;

import java.util.List;
import java.util.Optional;

public class LogicalBinaryFormula extends BinaryFormula {
  LogicalBinaryFormulaType logicalBinaryFormulaType;

  public LogicalBinaryFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    LogicalBinaryFormulaType logicalBinaryFormulaType
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.logicalBinaryFormulaType = logicalBinaryFormulaType;
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
    }

    return Optional.of(new PrimitiveType(PrimitiveTypeType.BOOLEAN));
  }

  @Override
  public String translate() {
    if (ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerRouteSymbolTable.class).getCurrentQuantifierVariableTypes().isEmpty()) {
      throw new RuntimeException("Could not directly translate logical binary formula");
    }

    return String.format(
      "%s%s %s %s",
      (this.logicalBinaryFormulaType == LogicalBinaryFormulaType.IMPLICATION) ? "!" : "",
      this.leftFormula.translate(),
      (this.logicalBinaryFormulaType == LogicalBinaryFormulaType.IMPLICATION) ? "||" : this.translateOperator(),
      this.rightFormula.translate()
    );
  }

  private String translateOperator() {
    return switch (this.logicalBinaryFormulaType) {
      case AND -> "&&";
      case OR -> "||";
      case IMPLICATION -> throw new RuntimeException("Could not generate monitor: unhandled implication");
    };
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return String.format(
      "%spre[%s] %s pre[%s]",
      (this.logicalBinaryFormulaType == LogicalBinaryFormulaType.IMPLICATION) ? "!" : "",
      subFormulas.indexOf(this.getLeftFormula()),
      (this.logicalBinaryFormulaType == LogicalBinaryFormulaType.IMPLICATION) ? "||" : this.translateOperator(),
      subFormulas.indexOf(this.getRightFormula())
    );
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return String.format(
      "%snow[%s] %s now[%s]",
      (this.logicalBinaryFormulaType == LogicalBinaryFormulaType.IMPLICATION) ? "!" : "",
      subFormulas.indexOf(this.getLeftFormula()),
      (this.logicalBinaryFormulaType == LogicalBinaryFormulaType.IMPLICATION) ? "||" : this.translateOperator(),
      subFormulas.indexOf(this.getRightFormula())
    );
  }
}
