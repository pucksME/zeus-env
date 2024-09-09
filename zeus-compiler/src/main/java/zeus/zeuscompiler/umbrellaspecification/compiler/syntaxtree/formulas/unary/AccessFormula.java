package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.IdentifierFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.exceptions.semanticanalysis.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class AccessFormula extends UnaryFormula {
  public AccessFormula(int line, int linePosition, Formula formula) {
    super(line, linePosition, formula);
  }

  private void buildIdentifiers(List<String> identifiers) {
    if (this.formula instanceof IdentifierFormula) {
      identifiers.add(((IdentifierFormula) this.formula).getId());
      return;
    }

    if (!(this.formula instanceof AccessFormula)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.formula.getLine(),
        this.formula.getLinePosition(),
        new UnknownIdentifierException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    this.buildIdentifiers(identifiers);
  }

  private List<String> buildIdentifiers() {
    List<String> identifiers = new ArrayList<>();
    buildIdentifiers(identifiers);
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

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }

  @Override
  public Optional<Type> evaluateType() {
    return Optional.empty();
  }
}
