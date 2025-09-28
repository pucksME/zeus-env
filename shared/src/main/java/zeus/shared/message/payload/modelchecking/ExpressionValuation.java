package zeus.shared.message.payload.modelchecking;

public class ExpressionValuation extends Valuation {
  private final ExpressionIdentifier expressionIdentifier;

  public ExpressionValuation(ExpressionIdentifier expressionIdentifier) {
    super(false);
    this.expressionIdentifier = expressionIdentifier;
  }

  public ExpressionValuation(boolean value, ExpressionIdentifier expressionIdentifier) {
    super(value);
    this.expressionIdentifier = expressionIdentifier;
  }

  public ExpressionIdentifier getExpressionIdentifier() {
    return expressionIdentifier;
  }

  @Override
  public int hashCode() {
    return expressionIdentifier.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExpressionValuation &&
      ((ExpressionValuation) obj).expressionIdentifier.equals(expressionIdentifier);
  }
}
