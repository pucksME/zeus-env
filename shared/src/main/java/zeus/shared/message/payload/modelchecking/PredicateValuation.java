package zeus.shared.message.payload.modelchecking;

import java.util.UUID;

public class PredicateValuation {
  UUID predicateUuid;
  boolean value;

  public PredicateValuation(UUID predicateUuid, boolean value) {
    this.predicateUuid = predicateUuid;
    this.value = value;
  }

  @Override
  public int hashCode() {
    return this.predicateUuid.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof PredicateValuation) && this.predicateUuid.equals(((PredicateValuation) obj).predicateUuid);
  }

  public boolean getValue() {
    return value;
  }
}
