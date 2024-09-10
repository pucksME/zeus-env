package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

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

public class ArithmeticBinaryFormula extends BinaryFormula {
  ArithmeticBinaryFormulaType arithmeticBinaryFormulaType;

  public ArithmeticBinaryFormula(
    int line,
    int linePosition,
    Formula leftFormula,
    Formula rightFormula,
    ArithmeticBinaryFormulaType arithmeticBinaryFormulaType
  ) {
    super(line, linePosition, leftFormula, rightFormula);
    this.arithmeticBinaryFormulaType = arithmeticBinaryFormulaType;
  }

  @Override
  public void check() {

  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<Type> leftFormulaTypeOptional = this.leftFormula.evaluateType();

    if (leftFormulaTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type leftFormulaType = leftFormulaTypeOptional.get();

    if (!(leftFormulaType instanceof PrimitiveType) ||
      (((PrimitiveType) leftFormulaType).getType() != PrimitiveTypeType.INT &&
        ((PrimitiveType) leftFormulaType).getType() != PrimitiveTypeType.FLOAT)) {
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
}
