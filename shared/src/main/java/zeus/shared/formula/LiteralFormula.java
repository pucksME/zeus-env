package zeus.shared.formula;

public abstract class LiteralFormula<T> extends Formula {
  T value;

  public LiteralFormula(T value) {
    this.value = value;
  }
}
