package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.CodeModuleComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnsupportedCodeModuleComponentsException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;

public class ResponseCodeModule extends ClientCodeModule {
  public ResponseCodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition, id, description);
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (!this.head.outputs.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedCodeModuleComponentsException(this.getId(), CodeModuleComponent.OUTPUT),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    if (!this.head.configs.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedCodeModuleComponentsException(this.getId(), CodeModuleComponent.CONFIG),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    if (!this.body.getBodyComponents().isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedCodeModuleComponentsException(this.getId(), CodeModuleComponent.BODY),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    Input bodyInput = this.head.inputs.getOrDefault("body", null);

    if (bodyInput != null &&
      !(bodyInput.getType() instanceof ObjectType) &&
      !(bodyInput.getType() instanceof IdType)) {
      compilerErrors.add(new CompilerError(
        bodyInput.getLine(),
        bodyInput.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    super.checkTypes(symbolTable, compilerErrors);
  }
}
