package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Input;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownCodeModulePortException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class CodeModuleInputExpression extends CodeModulePortExpression {
  String inputId;

  public CodeModuleInputExpression(int line, int linePosition, String codeModuleId, String inputId) {
    super(line, linePosition, codeModuleId);
    this.inputId = inputId;
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<ClientCodeModule> codeModuleOptional = this.getCodeModule(symbolTable, compilerErrors);

    if (codeModuleOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Input> inputOptional = codeModuleOptional.get().getInput(this.inputId);
    if (inputOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownCodeModulePortException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(inputOptional.get().getType());
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.inputId;
    };
  }

  public String getInputId() {
    return inputId;
  }
}
