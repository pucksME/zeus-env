package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.List;
import java.util.Optional;

public class AccessListFormula extends BinaryFormula {
  String id;

  public AccessListFormula(int line, int linePosition, Formula leftFormula, Formula rightFormula, String id) {
    super(line, linePosition, leftFormula, rightFormula);
    this.id = id;
  }

  @Override
  public Optional<Type> evaluateType() {
    return Optional.empty();
  }

  @Override
  public void check() {
  }

  public String translateIndexAccess() {
    return this.leftFormula.translate();
  }

  @Override
  public String translate() {
    throw new RuntimeException("Could not directly translate access list formula");
  }

  @Override
  public String translatePre(List<Formula> subFormulas) {
    return this.translate();
  }

  @Override
  public String translateNow(List<Formula> subFormulas) {
    return this.translate();
  }

  public String getId() {
    return id;
  }

  public Optional<Formula> getNextAccessFormula() {
    return Optional.ofNullable(this.rightFormula);
  }
}
