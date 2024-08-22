package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.ternary;

import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public abstract class TernaryExpression extends Expression {
  Expression firstExpression;
  Expression secondExpression;
  Expression thirdExpression;
  TernaryExpressionType type;

  public TernaryExpression(
    int line,
    int linePosition,
    Expression firstExpression,
    Expression secondExpression,
    Expression thirdExpression,
    TernaryExpressionType type
  ) {
    super(line, linePosition);
    this.firstExpression = firstExpression;
    this.secondExpression = secondExpression;
    this.thirdExpression = thirdExpression;
    this.type = type;
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> conditionTypeOptional = this.firstExpression.evaluateType(symbolTable, compilerErrors);

    if (conditionTypeOptional.isEmpty()) {
      return;
    }

    Type conditionType = conditionTypeOptional.get();

    if (!(conditionType instanceof PrimitiveType) || ((PrimitiveType) conditionType).getType() != LiteralType.BOOLEAN) {
      compilerErrors.add(new CompilerError(
        this.firstExpression.getLine(),
        this.firstExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> thenTypeOptional = this.secondExpression.evaluateType(symbolTable, compilerErrors);

    if (thenTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Type> elseTypeOptional = this.thirdExpression.evaluateType(symbolTable, compilerErrors);

    if (elseTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type thenType = thenTypeOptional.get();
    Type elseType = elseTypeOptional.get();

    if (!thenType.equals(elseType)) {
      compilerErrors.add(new CompilerError(
        this.thirdExpression.getLine(),
        this.thirdExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(thenType);
  }
}
