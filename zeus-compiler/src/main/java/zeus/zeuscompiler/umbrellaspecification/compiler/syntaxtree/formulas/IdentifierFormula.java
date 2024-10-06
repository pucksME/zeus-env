package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

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

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }

  public String getId() {
    return id;
  }

  @Override
  public Optional<Type> evaluateType() {
    ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
      this.getLine(),
      this.getLinePosition(),
      new UnknownIdentifierException(),
      CompilerPhase.TYPE_CHECKER
    ));
    return Optional.empty();
  }
}
