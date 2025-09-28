package zeus.shared.message.payload.modelchecking;

import java.util.Objects;
import java.util.UUID;

public class ExpressionIdentifier {
  Location location;
  UUID predicateUuid;

  public ExpressionIdentifier(Location location) {
    this.location = location;
  }

  public ExpressionIdentifier(Location location, UUID predicateUuid) {
    this(location);
    this.predicateUuid = predicateUuid;
  }

  @Override
  public int hashCode() {
    return (this.predicateUuid == null)
      ? this.location.hashCode()
      : String.format("%d,%d", this.location.hashCode(), this.predicateUuid.hashCode()).hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ExpressionIdentifier &&
      this.location.equals(((ExpressionIdentifier) obj).location) &&
      Objects.equals(this.predicateUuid, ((ExpressionIdentifier) obj).predicateUuid);
  }
}
