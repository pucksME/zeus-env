package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.CodeModuleComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnsupportedCodeModuleComponentsException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;

import java.util.List;
import java.util.Optional;


public class ResponseCodeModule extends RoutingCodeModule {
  public ResponseCodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition, id, description);
  }

  @Override
  public void checkTypes() {
    if (!this.head.outputs.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedCodeModuleComponentsException(this.getId(), CodeModuleComponent.OUTPUT),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    if (!this.head.configs.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedCodeModuleComponentsException(this.getId(), CodeModuleComponent.CONFIG),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    if (!this.body.getBodyComponents().isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
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
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        bodyInput.getLine(),
        bodyInput.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    super.checkTypes();
  }

  public Optional<Type> evaluateInputType(List<String> identifiers) {
    Optional<Input> inputOptional = this.getInput(identifiers.get(0));
    identifiers.remove(0);

    if (inputOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return this.evaluatePortType(inputOptional.get(), identifiers);
  }
}
