package zeus.shared.message.payload.modelchecking;

public class ExpressionValuation extends Valuation {
  private Location location;

  public ExpressionValuation(boolean value, Location location) {
    super(value);
    this.location = location;
  }

  @Override
  public int hashCode() {
    return location.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExpressionValuation && ((ExpressionValuation) obj).location.equals(location);
  }
}
