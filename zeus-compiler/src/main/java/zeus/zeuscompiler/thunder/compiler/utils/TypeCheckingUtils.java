package zeus.zeuscompiler.thunder.compiler.utils;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary.BinaryExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary.BinaryExpressionType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

import java.util.List;
import java.util.Optional;

public abstract class TypeCheckingUtils {
  public static Optional<Type> evaluateTypeNumericExpression(
    SymbolTable symbolTable,
    List<CompilerError> compilerErrors,
    BinaryExpression binaryExpression
  ) {
    Optional<Type> leftExpressionTypeOptional = binaryExpression.getLeftExpression().evaluateType(symbolTable, compilerErrors);

    if (leftExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> rightExpressionTypeOptional = binaryExpression.getRightExpression().evaluateType(symbolTable, compilerErrors);

    if (rightExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type leftExpressionType = leftExpressionTypeOptional.get();
    Type rightExpressionType = rightExpressionTypeOptional.get();

    if (!(leftExpressionType instanceof PrimitiveType) ||
      (((PrimitiveType) leftExpressionType).getType() != LiteralType.INT &&
        ((PrimitiveType) leftExpressionType).getType() != LiteralType.FLOAT)) {
      compilerErrors.add(new CompilerError(
        binaryExpression.getLine(),
        binaryExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    if (!(rightExpressionType instanceof PrimitiveType) ||
      (((PrimitiveType) rightExpressionType).getType() != LiteralType.INT &&
        ((PrimitiveType) rightExpressionType).getType() != LiteralType.FLOAT)) {
      compilerErrors.add(new CompilerError(
        binaryExpression.getLine(),
        binaryExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    BinaryExpressionType binaryExpressionType = binaryExpression.getType();

    if (binaryExpressionType == BinaryExpressionType.GREATER_THAN ||
      binaryExpressionType == BinaryExpressionType.GREATER_EQUAL_THAN ||
      binaryExpressionType == BinaryExpressionType.LESS_THAN ||
      binaryExpressionType == BinaryExpressionType.LESS_EQUAL_THAN) {
      return Optional.of(new PrimitiveType(LiteralType.BOOLEAN));
    }

    return Optional.of(new PrimitiveType(
      (((PrimitiveType) leftExpressionType).getType() == LiteralType.INT &&
        ((PrimitiveType) rightExpressionType).getType() == LiteralType.INT)
        ? LiteralType.INT
        : LiteralType.FLOAT
    ));
  }

  public static Optional<Type> evaluateTypeLogicExpression(
    SymbolTable symbolTable,
    List<CompilerError> compilerErrors,
    BinaryExpression binaryExpression
  ) {
    Optional<Type> leftExpressionTypeOptional = binaryExpression.getLeftExpression().evaluateType(symbolTable, compilerErrors);

    if (leftExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> rightExpressionTypeOptional = binaryExpression.getRightExpression().evaluateType(symbolTable, compilerErrors);

    if (rightExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type leftExpressionType = leftExpressionTypeOptional.get();
    Type rightExpressionType = rightExpressionTypeOptional.get();

    if (!(leftExpressionType instanceof PrimitiveType) ||
      ((PrimitiveType) leftExpressionType).getType() != LiteralType.BOOLEAN) {
      compilerErrors.add(new CompilerError(
        binaryExpression.getLine(),
        binaryExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    if (!(rightExpressionType instanceof PrimitiveType) ||
      ((PrimitiveType) rightExpressionType).getType() != LiteralType.BOOLEAN) {
      compilerErrors.add(new CompilerError(
        binaryExpression.getLine(),
        binaryExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(new PrimitiveType(LiteralType.BOOLEAN));
  }

  public static Optional<Type> evaluateTypeCompareExpression(
    SymbolTable symbolTable,
    List<CompilerError> compilerErrors,
    BinaryExpression binaryExpression
  ) {
    Optional<Type> leftExpressionTypeOptional = binaryExpression.getLeftExpression().evaluateType(
      symbolTable,
      compilerErrors
    );

    if (leftExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> rightExpressionTypeOptional = binaryExpression.getRightExpression().evaluateType(
      symbolTable,
      compilerErrors
    );

    if (rightExpressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type rightExpressionType = rightExpressionTypeOptional.get();

    if (!leftExpressionTypeOptional.get().compatible(symbolTable, compilerErrors, rightExpressionType)) {
      compilerErrors.add(new CompilerError(
        binaryExpression.getRightExpression().getLine(),
        binaryExpression.getRightExpression().getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(new PrimitiveType(LiteralType.BOOLEAN));
  }
}
