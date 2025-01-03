package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary.AccessFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class QuantifierFormula extends BinaryFormula {
  String id;
  QuantifierFormulaType type;

  public QuantifierFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    String id,
    QuantifierFormulaType type
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.id = id;
    this.type = type;
  }

  @Override
  public void check() {
    if (!(this.leftFormula instanceof AccessFormula)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> leftFormulaThunderTypeOptional = (
      (AccessFormula) this.leftFormula
    ).evaluateThunderType();

    if (leftFormulaThunderTypeOptional.isEmpty()) {
      return;
    }

    zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type leftFormulaThunderType =
      leftFormulaThunderTypeOptional.get();

    if (!(leftFormulaThunderType instanceof zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerRouteSymbolTable.class).setCurrentQuantifierVariableTypes(Map.of(
        this.id,
        ((ListType) leftFormulaThunderType).getType()
      ));

    Optional<Type> rightFormulaTypeOptional = this.rightFormula.evaluateType();


    if (rightFormulaTypeOptional.isEmpty() ||
      !(rightFormulaTypeOptional.get() instanceof PrimitiveType) ||
      ((PrimitiveType) rightFormulaTypeOptional.get()).getType() != PrimitiveTypeType.BOOLEAN ||
      rightFormula.getSubFormulas().stream().anyMatch(Formula::isTemporal)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.rightFormula.getLine(),
        this.rightFormula.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerRouteSymbolTable.class).resetCurrentQuantifierVariableTypes();
  }

  @Override
  public Optional<Type> evaluateType() {
    return Optional.of(new PrimitiveType(PrimitiveTypeType.BOOLEAN));
  }

  @Override
  public List<Formula> getSubFormulas() {
    return new ArrayList<>();
  }

  @Override
  public String translate() {
    return "";
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return "";
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return "";
  }
}
