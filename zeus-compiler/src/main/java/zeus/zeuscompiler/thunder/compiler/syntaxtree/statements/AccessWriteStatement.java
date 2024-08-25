package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.MapType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AccessWriteStatement extends Statement {
  Expression containerExpression;
  Expression locationExpression;
  Expression writeExpression;

  public AccessWriteStatement(
    int line,
    int linePosition,
    Expression containerExpression,
    Expression locationExpression,
    Expression writeExpression
  ) {
    super(line, linePosition);
    this.containerExpression = containerExpression;
    this.locationExpression = locationExpression;
    this.writeExpression = writeExpression;
  }

  @Override
  public void checkTypes() {
    Optional<Type> containerTypeOptional = this.containerExpression.evaluateType();

    if (containerTypeOptional.isEmpty()) {
      return;
    }

    Type containerType = containerTypeOptional.get();

    if (!(containerType instanceof ListType) && !(containerType instanceof MapType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.containerExpression.getLine(),
        this.containerExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> locationTypeOptional = this.locationExpression.evaluateType();

    if (locationTypeOptional.isEmpty()) {
      return;
    }

    Type locationType = locationTypeOptional.get();

    if (containerType instanceof ListType &&
      (!(locationType instanceof PrimitiveType) || ((PrimitiveType) locationType).getType() != LiteralType.INT)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.locationExpression.getLine(),
        this.locationExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (containerType instanceof MapType &&
      !locationType.compatible(((MapType) containerType).getKeyType())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.locationExpression.getLine(),
        this.locationExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> writeTypeOptional = this.writeExpression.evaluateType();

    if (writeTypeOptional.isEmpty()) {
      return;
    }

    Type writeType = writeTypeOptional.get();

    if (containerType instanceof ListType &&
      !writeType.compatible(((ListType) containerType).getType())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.writeExpression.getLine(),
        this.writeExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (containerType instanceof MapType &&
      !writeType.compatible(((MapType) containerType).getValueType())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.writeExpression.getLine(),
        this.writeExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        Optional<Type> typeOptional = this.containerExpression.evaluateType();

        if (typeOptional.isPresent() && typeOptional.get() instanceof MapType) {
          yield String.format(
            "%s.set(%s, %s)",
            this.containerExpression.translate(depth, exportTarget),
            this.locationExpression.translate(depth, exportTarget),
            this.writeExpression.translate(depth, exportTarget)
          );
        }

        yield String.format(
          "%s[%s] = %s;",
          this.containerExpression.translate(depth, exportTarget),
          this.locationExpression.translate(depth, exportTarget),
          this.writeExpression.translate(depth, exportTarget)
        );
      }
    };
  }
}
