package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.TypeInformation;
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

public class AccessWriteObjectStatement extends Statement {
  Expression objectExpression;
  String propertyId;
  Expression writeExpression;

  public AccessWriteObjectStatement(
    int line,
    int linePosition,
    Expression objectExpression,
    String propertyId,
    Expression writeExpression
  ) {
    super(line, linePosition);
    this.objectExpression = objectExpression;
    this.propertyId = propertyId;
    this.writeExpression = writeExpression;
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> objectTypeOptional = this.objectExpression.evaluateType(symbolTable, compilerErrors);

    if (objectTypeOptional.isEmpty()) {
      return;
    }

    Type objectType = objectTypeOptional.get();

    if (objectType instanceof IdType) {
      Optional<TypeInformation> typeInformationOptional = symbolTable.getType(
        symbolTable.getCurrentCodeModule(),
        ((IdType) objectType).getId()
      );

      if (typeInformationOptional.isEmpty()) {
        compilerErrors.add(new CompilerError(
          this.objectExpression.getLine(),
          this.objectExpression.getLinePosition(),
          new UnknownTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));

        return;
      }

      objectType = typeInformationOptional.get().getType();
    }

    if (!(objectType instanceof ObjectType)) {
      compilerErrors.add(new CompilerError(
        this.objectExpression.getLine(),
        this.objectExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> propertyTypeOptional = ((ObjectType) objectType).getPropertyType(this.propertyId);

    if (propertyTypeOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownObjectPropertyException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> writeExpressionTypeOptional = this.writeExpression.evaluateType(symbolTable, compilerErrors);

    if (writeExpressionTypeOptional.isEmpty()) {
      return;
    }

    if (!propertyTypeOptional.get().compatible(symbolTable, compilerErrors, writeExpressionTypeOptional.get())) {
      compilerErrors.add(new CompilerError(
        writeExpression.getLine(),
        writeExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s.%s = %s;",
        this.objectExpression.translate(symbolTable, depth, exportTarget),
        this.propertyId, this.writeExpression.translate(symbolTable, depth, exportTarget)
      );
    };
  }
}
