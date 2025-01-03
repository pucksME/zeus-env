package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class IdentifierFormula extends Formula {
  String id;

  public IdentifierFormula(int line, int linePosition, String id) {
    super(line, linePosition);
    this.id = id;
  }

  @Override
  public void check() {
    this.evaluateType();
  }

  public String getId() {
    return id;
  }

  @Override
  public Optional<Type> evaluateType() {
    Optional<Map<String, zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type>> quantifierVariableTypesOptional =
      ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(ServerRouteSymbolTable.class).getCurrentQuantifierVariableTypes();

    if (quantifierVariableTypesOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    Map<String, zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type> quantifierVariableTypes =
      quantifierVariableTypesOptional.get();

    zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type quantifierVariableType = quantifierVariableTypes.get(id);

    if (quantifierVariableType == null) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return Optional.empty();
    }

    if (!(quantifierVariableType instanceof zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType)) {
      return Optional.empty();
    }

    return Optional.ofNullable(PrimitiveType.fromThunderType(
      (zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType) quantifierVariableType
    ).orElse(null));
  }

  @Override
  public List<Formula> getSubFormulas() {
    return new ArrayList<>();
  }

  @Override
  public boolean accessesResponse() {
    return false;
  }

  @Override
  public String translate() {
    throw new RuntimeException("Could not directly translate identifier formula");
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    throw new RuntimeException("Could not translate identifier formula (pre)");
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    throw new RuntimeException("Could not translate identifier formula (now)");
  }
}
