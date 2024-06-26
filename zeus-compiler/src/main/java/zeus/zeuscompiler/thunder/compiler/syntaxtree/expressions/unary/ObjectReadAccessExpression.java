package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownObjectPropertyException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class ObjectReadAccessExpression extends UnaryExpression {
  String propertyId;

  public ObjectReadAccessExpression(
    int line,
    int linePosition,
    Expression expression,
    String propertyId,
    UnaryExpressionType unaryExpressionType
  ) {
    super(line, linePosition, expression, unaryExpressionType);
    this.propertyId = propertyId;
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> expressionTypeOptional = this.expression.evaluateType(symbolTable, compilerErrors);

    if (expressionTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    Type expressionType = expressionTypeOptional.get();

    if (expressionType instanceof IdType) {
      Optional<TypeInformation> typeInformationOptional = symbolTable.getType(
        symbolTable.getCurrentCodeModule(),
        ((IdType) expressionType).getId()
      );

      if (typeInformationOptional.isEmpty()) {
        compilerErrors.add(new CompilerError(
          this.expression.getLine(),
          this.expression.getLinePosition(),
          new UnknownTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      expressionType = typeInformationOptional.get().getType();
    }

    if (!(expressionType instanceof ObjectType)) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    Optional<Type> propertyTypeOptional = ((ObjectType) expressionType).getPropertyType(this.propertyId);

    if (propertyTypeOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownObjectPropertyException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return propertyTypeOptional;
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s.%s",
        this.expression.translate(symbolTable, depth, exportTarget),
        this.propertyId
      );
    };
  }
}
