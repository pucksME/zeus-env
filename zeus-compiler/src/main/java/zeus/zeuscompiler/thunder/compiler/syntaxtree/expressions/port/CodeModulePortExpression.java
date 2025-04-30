package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port;

import zeus.shared.formula.Formula;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleCodeModuleException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownCodeModuleException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Optional;

public abstract class CodeModulePortExpression extends Expression {
  String codeModuleId;

  public CodeModulePortExpression(int line, int linePosition, String codeModuleId) {
    super(line, linePosition);
    this.codeModuleId = codeModuleId;
  }

  public Optional<ClientCodeModule> getCodeModule() {
    Optional<CodeModule> codeModuleOptional = ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).getCodeModules().getCodeModule(this.codeModuleId);

    if (codeModuleOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownCodeModuleException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    CodeModule codeModule = codeModuleOptional.get();
    if (!(codeModule instanceof ClientCodeModule)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleCodeModuleException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of((ClientCodeModule) codeModule);
  }

  public String getCodeModuleId() {
    return codeModuleId;
  }

  @Override
  public Formula toFormula() {
    throw new RuntimeException("Could not convert code module port expression to formula: not implemented yet");
  }
}
