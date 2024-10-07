package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.CodeModuleComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnsupportedCodeModuleComponentsException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;

import java.util.List;
import java.util.Optional;

public class RequestCodeModule extends RoutingCodeModule {
  public RequestCodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition, id, description);
  }

  @Override
  public void checkTypes() {
    if (!this.head.inputs.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedCodeModuleComponentsException(this.getId(), CodeModuleComponent.INPUT),
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

    Output urlOutput = this.head.outputs.getOrDefault("url", null);

    if (urlOutput != null &&
      !(urlOutput.getType() instanceof ObjectType) &&
      !(urlOutput.getType() instanceof IdType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        urlOutput.getLine(),
        urlOutput.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    Output bodyOutput = this.head.outputs.getOrDefault("body", null);

    if (bodyOutput != null &&
      !(bodyOutput.getType() instanceof ObjectType) &&
      !(bodyOutput.getType() instanceof IdType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        bodyOutput.getLine(),
        bodyOutput.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    super.checkTypes();
  }

  public Optional<Type> evaluateOutputType(List<String> identifiers) {
    Optional<Output> outputOptional = this.getOutput(identifiers.get(0));
    identifiers.remove(0);

    if (outputOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return this.evaluatePortType(outputOptional.get(), identifiers);
  }
}
