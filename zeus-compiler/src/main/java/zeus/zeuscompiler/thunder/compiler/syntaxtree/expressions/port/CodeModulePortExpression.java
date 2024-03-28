package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port;

import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleCodeModuleException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownCodeModuleException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public abstract class CodeModulePortExpression extends Expression {
  String codeModuleId;

  public CodeModulePortExpression(int line, int linePosition, String codeModuleId) {
    super(line, linePosition);
    this.codeModuleId = codeModuleId;
  }

  public Optional<ClientCodeModule> getCodeModule(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<CodeModule> codeModuleOptional = symbolTable.getCodeModules().getCodeModule(this.codeModuleId);
    if (codeModuleOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownCodeModuleException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    CodeModule codeModule = codeModuleOptional.get();
    if (!(codeModule instanceof ClientCodeModule)) {
      compilerErrors.add(new CompilerError(
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
}
