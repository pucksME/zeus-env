package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ResponseCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RoutingCodeModule;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownRoutingCodeModuleException;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnsupportedTypeException;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.IdentifierFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AccessFormula extends UnaryFormula {
  String id;

  public AccessFormula(int line, int linePosition, Formula formula, String id) {
    super(line, linePosition, formula);
    this.id = id;
  }

  private void buildIdentifiers(Formula formula, List<String> identifiers) {
    if (formula instanceof IdentifierFormula) {
      identifiers.add(((IdentifierFormula) formula).getId());
      return;
    }

    if (!(formula instanceof AccessFormula)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.formula.getLine(),
        this.formula.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    identifiers.add(((AccessFormula) formula).id);
    this.buildIdentifiers(((AccessFormula) formula).formula, identifiers);
  }

  public List<String> buildIdentifiers() {
    List<String> identifiers = new ArrayList<>();
    buildIdentifiers(this, identifiers);
    return identifiers;
  }

  @Override
  public void check() {
    int errorCount = ServiceProvider.provide(CompilerErrorService.class).getErrors().size();
    List<String> identifiers = this.buildIdentifiers();

    if (errorCount < ServiceProvider.provide(CompilerErrorService.class).getErrors().size()) {
      return;
    }

    if (identifiers.size() < 3) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.formula.getLine(),
        this.formula.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (identifiers.get(0).equals("request")) {
      // TODO: handle request code module access
      return;
    }

    if (identifiers.get(0).equals("response")) {
      // TODO: handle response code module access
      return;
    }

    ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
      this.formula.getLine(),
      this.formula.getLinePosition(),
      new UnknownIdentifierException(),
      CompilerPhase.TYPE_CHECKER
    ));
  }

  private Optional<Type> evaluateThunderType(zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type thunderType) {
    if (!(thunderType instanceof zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    Optional<PrimitiveType> primitiveTypeOptional = PrimitiveType.fromThunderType(
      (zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType) thunderType
    );

    if (primitiveTypeOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnsupportedTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return Optional.of(primitiveTypeOptional.get());
  }

  @Override
  public Optional<Type> evaluateType() {
    List<String> identifiers = this.buildIdentifiers();

    if (identifiers.get(0).equals("request")) {
      identifiers.remove(0);
      Optional<RequestCodeModule> requestCodeModuleOptional = ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(ServerRouteSymbolTable.class).getRoutingCodeModule(RequestCodeModule.class);

      if (requestCodeModuleOptional.isEmpty()) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new UnknownRoutingCodeModuleException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> thunderTypeOptional =
        requestCodeModuleOptional.get().evaluateOutputType(identifiers);

      if (thunderTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      return this.evaluateThunderType(thunderTypeOptional.get());
    }

    if (identifiers.get(0).equals("response")) {
      identifiers.remove(0);

      Optional<ResponseCodeModule> responseCodeModuleOptional = ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(ServerRouteSymbolTable.class).getRoutingCodeModule(ResponseCodeModule.class);

      if (responseCodeModuleOptional.isEmpty()) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new UnknownRoutingCodeModuleException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> thunderTypeOptional =
        responseCodeModuleOptional.get().evaluateInputType(identifiers);

      if (thunderTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      return this.evaluateThunderType(thunderTypeOptional.get());
    }

    ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
      this.getLine(),
      this.getLinePosition(),
      new UnknownRoutingCodeModuleException(),
      CompilerPhase.TYPE_CHECKER
    ));
    return Optional.empty();
  }
}
