package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.SymbolTable;
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
  public void checkTypes() {
    Optional<Type> objectTypeOptional = this.objectExpression.evaluateType();

    if (objectTypeOptional.isEmpty()) {
      return;
    }

    Type objectType = objectTypeOptional.get();

    if (objectType instanceof IdType) {
      Optional<TypeInformation> typeInformationOptional = ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(SymbolTable.class).getType(
          ServiceProvider
            .provide(SymbolTableService.class).getContextSymbolTableProvider()
            .provide(SymbolTable.class).getCurrentCodeModule(),
          ((IdType) objectType).getId()
      );

      if (typeInformationOptional.isEmpty()) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
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
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.objectExpression.getLine(),
        this.objectExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> propertyTypeOptional = ((ObjectType) objectType).getPropertyType(this.propertyId);

    if (propertyTypeOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownObjectPropertyException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> writeExpressionTypeOptional = this.writeExpression.evaluateType();

    if (writeExpressionTypeOptional.isEmpty()) {
      return;
    }

    if (!propertyTypeOptional.get().compatible(writeExpressionTypeOptional.get())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        writeExpression.getLine(),
        writeExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s.%s = %s;",
        this.objectExpression.translate(depth, exportTarget),
        this.propertyId, this.writeExpression.translate(depth, exportTarget)
      );
    };
  }
}
