package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ResponseCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownRoutingCodeModuleException;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnsupportedTypeException;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.AccessListFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveTypeType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.IdentifierFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class AccessFormula extends UnaryFormula {
  String id;

  public AccessFormula(int line, int linePosition, Formula formula, String id) {
    super(line, linePosition, formula);
    this.id = id;
  }

  private void buildIdentifiers(Formula formula, List<String> identifiers, boolean translate) {
    if (formula instanceof IdentifierFormula) {
      identifiers.add(((IdentifierFormula) formula).getId());
      return;
    }

    if (formula instanceof AccessListFormula) {
      identifiers.add((translate)
        ? String.format(
            "%s@\" + (%s) + \"",
            ((AccessListFormula) formula).getId(),
            ((AccessListFormula) formula).translateIndexAccess()
          )
        : String.format("%s", ((AccessListFormula) formula).getId()));

      if (!translate) {
        identifiers.add("[]");
      }

      Optional<Formula> nextAccessFormulaOptional = ((AccessListFormula) formula).getNextAccessFormula();
      if (nextAccessFormulaOptional.isPresent()) {
        this.buildIdentifiers(((AccessListFormula) formula).getRightFormula(), identifiers, translate);
      }

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
    this.buildIdentifiers(((AccessFormula) formula).formula, identifiers, translate);
  }

  public List<String> buildIdentifiers() {
    List<String> identifiers = new ArrayList<>();
    this.buildIdentifiers(this, identifiers, false);
    return identifiers;
  }

  public List<String> buildTranslatedIdentifiers() {
    List<String> identifiers = new ArrayList<>();
    this.buildIdentifiers(this, identifiers, true);
    return identifiers;
  }

  @Override
  public void check() {
    this.evaluateType();
  }

  private Optional<PrimitiveType> convertType(zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type thunderType) {
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

    return primitiveTypeOptional;
  }

  public Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> evaluateRoutingCodeModuleThunderType(
    List<String> identifiers
  ) {
    Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> thunderTypeOptional = Optional.empty();

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

      thunderTypeOptional = requestCodeModuleOptional.get().evaluateOutputType(identifiers);

      if (thunderTypeOptional.isEmpty()) {
        return Optional.empty();
      }
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

      thunderTypeOptional = responseCodeModuleOptional.get().evaluateInputType(identifiers);
    }



    return thunderTypeOptional;
  }

  private Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> evaluateQuantifierThunderType(List<String> identifiers) {
    Optional<Map<String, zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type>> quantifierVariableTypesOptional = ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerRouteSymbolTable.class).getCurrentQuantifierVariableTypes();

    if (quantifierVariableTypesOptional.isPresent()) {
      Map<String, zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> quantifierVariableTypes =
        quantifierVariableTypesOptional.get();

      zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type quantifierVariableType =
        quantifierVariableTypes.get(identifiers.get(0));

      if (quantifierVariableType == null) {
        return Optional.empty();
      }

      return quantifierVariableType.getType(identifiers.subList(1, identifiers.size()));
    }

    return Optional.empty();
  }

  public Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> evaluateThunderType() {
    List<String> identifiers = this.buildIdentifiers();
    Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> thunderTypeOptional =
      this.evaluateRoutingCodeModuleThunderType(new ArrayList<>(identifiers));

    if (thunderTypeOptional.isPresent()) {
      return thunderTypeOptional;
    }

    thunderTypeOptional = this.evaluateQuantifierThunderType(new ArrayList<>(identifiers));

    if (thunderTypeOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    return thunderTypeOptional;
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> thunderTypeOptional = this.evaluateThunderType();

    if (thunderTypeOptional.isEmpty()) {
      return Optional.empty();
    }

    return Optional.ofNullable(this.convertType(thunderTypeOptional.get()).orElse(null));
  }

  @Override
  public List<Formula> getSubFormulas() {
    Optional<Type> typeOptional = this.evaluateType();

    if (typeOptional.isEmpty()) {
      throw new RuntimeException("Could not get sub formulas of access formula: type not present");
    }

    Type type = typeOptional.get();

    if (!(type instanceof PrimitiveType) || ((PrimitiveType) type).getType() != PrimitiveTypeType.BOOLEAN) {
      throw new RuntimeException("Could not get sub formulas of access formula: invalid type");
    }

    return new ArrayList<>(List.of(this));
  }

  @Override
  public boolean accessesResponse() {
    return this.id.equals("response");
  }

  public String translateQuantifierList() {
    return String.format(
      "this.getVariableValueAsList(\"%s\")",
      String.join(".", this.buildTranslatedIdentifiers())
    );
  }

  private String translatePrimitiveTypeAccess(Type type) {
    if (!(type instanceof PrimitiveType)) {
      throw new RuntimeException("Could not directly translate access formula: unsupported type");
    }

    return switch (((PrimitiveType) type).getType()) {
        case INT -> "this.getVariableValueAsInt";
        case FLOAT -> "this.getVariableValueAsFloat";
        case STRING -> "this.getVariableValueAsString";
        case BOOLEAN -> "this.getVariableValueAsBoolean";
      };
  }

  public String translateQuantifierVariableAccess() {
    return String.format(
      "%s(\"%s\", Request.getVariables(\"%s\", %s))",
      this.translatePrimitiveTypeAccess(this.evaluateType().orElse(null)),
      String.join(".", this.buildTranslatedIdentifiers()),
      this.id,
      this.id
    );
  }

  @Override
  public String translate() {
    Optional<Map<String, zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type>> quantifierVariableTypes =
      ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(ServerRouteSymbolTable.class).getCurrentQuantifierVariableTypes();

    if (quantifierVariableTypes.isPresent()) {
      if (quantifierVariableTypes.get().containsKey(this.id)) {
        return this.translateQuantifierVariableAccess();
      }

      Optional<zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> typeOptional = this.evaluateThunderType();

      if (typeOptional.isPresent() && typeOptional.get() instanceof ListType) {
        return this.translateQuantifierList();
      }
    }

    Optional<Type> typeOptional = this.evaluateType();

    if (typeOptional.isEmpty()) {
      throw new RuntimeException("Could not directly translate access formula: type evaluation failed");
    }

    return String.format(
      "%s(\"%s\")",
      this.translatePrimitiveTypeAccess(typeOptional.get()),
      String.join(".", this.buildTranslatedIdentifiers())
    );
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return this.translate();
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return this.translatePre(subFormulas);
  }

  public String getId() {
    return id;
  }
}
