package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.CodeModuleInputExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.CodeModuleOutputExpression;
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
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> codeModuleInputTypeOptional = this.codeModuleInputExpression.evaluateType(
      symbolTable,
      compilerErrors
    );

    if (codeModuleInputTypeOptional.isEmpty()) {
      return;
    }

    Optional<Type> codeModuleOutputTypeOptional = this.codeModuleOutputExpression.evaluateType(
      symbolTable,
      compilerErrors
    );

    if (codeModuleOutputTypeOptional.isEmpty()) {
      return;
    }

    if (!codeModuleInputTypeOptional.get().compatible(
      symbolTable,
      compilerErrors,
      codeModuleOutputTypeOptional.get()
    )) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
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
