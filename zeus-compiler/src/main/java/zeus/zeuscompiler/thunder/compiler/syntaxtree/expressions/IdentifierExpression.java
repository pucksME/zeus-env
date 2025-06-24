package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.shared.formula.*;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.SymbolTable;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownVariableException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Map;
import java.util.Optional;

public class IdentifierExpression extends Expression {
  String id;

  public IdentifierExpression(int line, int linePosition, String id) {
    super(line, linePosition);
    this.id = id;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    int line = this.getLine();
    int linePosition = this.getLinePosition();
    Optional<VariableInformation> variableInformationOptional = ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).getVariable(
        ServiceProvider
          .provide(SymbolTableService.class).getContextSymbolTableProvider()
          .provide(SymbolTable.class).getCurrentCodeModule(),
        this.id,
        line,
        linePosition
    );

    if (variableInformationOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        line,
        linePosition,
        new UnknownVariableException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(variableInformationOptional.get().getType());
  }

  @Override
  public Formula toFormula(Map<String, VariableInformation> variables) {
    VariableInformation variableInformation = variables.get(this.id);

    if (variableInformation == null) {
      throw new RuntimeException("Could not convert identifier expression to formula: type evaluation failed");
    }

    Type type = variableInformation.getType();

    if (!(type instanceof PrimitiveType)) {
      throw new RuntimeException(String.format(
        "Could not convert identifier expression to formula: unsupported type \"%s\"",
        type
      ));
    }

    return switch (((PrimitiveType) type).getType()) {
      case INT -> new IntegerVariableFormula(this.id);
      case FLOAT -> new FloatVariableFormula(this.id);
      case BOOLEAN -> new BooleanVariableFormula(this.id);
      case STRING -> new StringVariableFormula(this.id);
      default -> throw new RuntimeException(String.format(
        "Could not convert identifier expression to formula: unsupported primitive type \"%s\"",
        ((PrimitiveType) type).getType()
      ));
    };
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.id;
    };
  }
}
