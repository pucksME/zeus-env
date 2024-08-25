package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class ConnectionStatement extends Statement {
  CodeModuleInputExpression codeModuleInputExpression;
  CodeModuleOutputExpression codeModuleOutputExpression;

  public ConnectionStatement(
    int line,
    int linePosition,
    CodeModuleInputExpression codeModuleInputExpression,
    CodeModuleOutputExpression codeModuleOutputExpression
  ) {
    super(line, linePosition);
    this.codeModuleInputExpression = codeModuleInputExpression;
    this.codeModuleOutputExpression = codeModuleOutputExpression;
  }

  @Override
  public void checkTypes() {
    Optional<Type> codeModuleInputTypeOptional = this.codeModuleInputExpression.evaluateType();

    if (codeModuleInputTypeOptional.isEmpty()) {
      return;
    }

    Optional<Type> codeModuleOutputTypeOptional = this.codeModuleOutputExpression.evaluateType();

    if (codeModuleOutputTypeOptional.isEmpty()) {
      return;
    }

    if (!codeModuleInputTypeOptional.get().compatible(codeModuleOutputTypeOptional.get())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> "";
    };
  }

  public CodeModuleInputExpression getCodeModuleInputExpression() {
    return codeModuleInputExpression;
  }

  public CodeModuleOutputExpression getCodeModuleOutputExpression() {
    return codeModuleOutputExpression;
  }
}
