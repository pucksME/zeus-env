package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

public abstract class BinaryFormula extends Formula {
  Formula leftFormula;
  Formula rightFormula;

  public BinaryFormula(int line, int linePosition, Formula leftFormula, Formula rightFormula) {
    super(line, linePosition);
    this.leftFormula = leftFormula;
    this.rightFormula = rightFormula;
  }
}
