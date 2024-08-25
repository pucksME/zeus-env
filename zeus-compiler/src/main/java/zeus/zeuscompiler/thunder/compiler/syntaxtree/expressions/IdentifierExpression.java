package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.SymbolTable;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownVariableException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
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
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.id;
    };
  }
}
