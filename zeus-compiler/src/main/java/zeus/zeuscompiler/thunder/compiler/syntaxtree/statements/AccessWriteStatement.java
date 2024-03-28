package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
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
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> containerTypeOptional = this.containerExpression.evaluateType(symbolTable, compilerErrors);

    if (containerTypeOptional.isEmpty()) {
      return;
    }

    Type containerType = containerTypeOptional.get();

    if (!(containerType instanceof ListType) && !(containerType instanceof MapType)) {
      compilerErrors.add(new CompilerError(
        this.containerExpression.getLine(),
        this.containerExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> locationTypeOptional = this.locationExpression.evaluateType(symbolTable, compilerErrors);

    if (locationTypeOptional.isEmpty()) {
      return;
    }

    Type locationType = locationTypeOptional.get();

    if (containerType instanceof ListType &&
      (!(locationType instanceof PrimitiveType) || ((PrimitiveType) locationType).getType() != LiteralType.INT)) {
      compilerErrors.add(new CompilerError(
        this.locationExpression.getLine(),
        this.locationExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (containerType instanceof MapType &&
      !locationType.compatible(symbolTable, compilerErrors, ((MapType) containerType).getKeyType())) {
      compilerErrors.add(new CompilerError(
        this.locationExpression.getLine(),
        this.locationExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> writeTypeOptional = this.writeExpression.evaluateType(symbolTable, compilerErrors);

    if (writeTypeOptional.isEmpty()) {
      return;
    }

    Type writeType = writeTypeOptional.get();

    if (containerType instanceof ListType &&
      !writeType.compatible(symbolTable, compilerErrors, ((ListType) containerType).getType())) {
      compilerErrors.add(new CompilerError(
        this.writeExpression.getLine(),
        this.writeExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (containerType instanceof MapType &&
      !writeType.compatible(symbolTable, compilerErrors, ((MapType) containerType).getValueType())) {
      compilerErrors.add(new CompilerError(
        this.writeExpression.getLine(),
        this.writeExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        Optional<Type> typeOptional = this.containerExpression.evaluateType(symbolTable, new ArrayList<>());

        if (typeOptional.isPresent() && typeOptional.get() instanceof MapType) {
          yield String.format(
            "%s.set(%s, %s)",
            this.containerExpression.translate(symbolTable, depth, exportTarget),
            this.locationExpression.translate(symbolTable, depth, exportTarget),
            this.writeExpression.translate(symbolTable, depth, exportTarget)
          );
        }

        yield String.format(
          "%s[%s] = %s;",
          this.containerExpression.translate(symbolTable, depth, exportTarget),
          this.locationExpression.translate(symbolTable, depth, exportTarget),
          this.writeExpression.translate(symbolTable, depth, exportTarget)
        );
      }
    };
  }
}
