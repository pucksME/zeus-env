package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Output;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownCodeModulePortException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Optional;

public class CodeModuleOutputExpression extends CodeModulePortExpression {
  String outputId;

  public CodeModuleOutputExpression(int line, int linePosition, String codeModuleId, String outputId) {
    super(line, linePosition, codeModuleId);
    this.outputId = outputId;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<ClientCodeModule> codeModuleOptional = this.getCodeModule();

    if (codeModuleOptional.isEmpty()) {
      return Optional.empty();
    }

    Optional<Output> outputOptional = codeModuleOptional.get().getOutput(this.outputId);
    if (outputOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownCodeModulePortException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(outputOptional.get().getType());
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format("%s_%s", this.codeModuleId, this.outputId);
    };
  }

  public String getOutputId() {
    return outputId;
  }
}
