package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.symboltable.VariableInformation;
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
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    int line = this.getLine();
    int linePosition = this.getLinePosition();
    Optional<VariableInformation> variableInformationOptional = symbolTable.getVariable(
      symbolTable.getCurrentCodeModule(),
      this.id,
      line,
      linePosition
    );

    if (variableInformationOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
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
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.id;
    };
  }
}
