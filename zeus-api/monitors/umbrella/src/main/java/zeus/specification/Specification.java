package zeus.specification;

import java.util.HashMap;
import java.util.Optional;

public abstract class Specification {
  Context context;
  Action action;

  public Specification(Context context, Action action) {
    this.context = context;
    this.action = action;
  }

  private Optional<String> getVariableValue(String identifier, HashMap<String, String> variables) {
    return Optional.ofNullable(variables.get(identifier));
  }

  public String getVariableValueAsString(String identifier, HashMap<String, String> variables) {
    return this.getVariableValue(identifier, variables).orElseThrow();
  }

  public int getVariableValueAsInt(String identifier, HashMap<String, String> variables) {
    return Integer.parseInt(this.getVariableValue(identifier, variables).orElseThrow());
  }

  public float getVariableValueAsFloat(String identifier, HashMap<String, String> variables) {
    return Float.parseFloat(this.getVariableValue(identifier, variables).orElseThrow());
  }

  public boolean getVariableValueAsBoolean(String identifier, HashMap<String, String> variables) {
    String variableValue = this.getVariableValue(identifier, variables).orElseThrow();
    if (variableValue.equals("true")) {
      return true;
    }

    if (variableValue.equals("false")) {
      return false;
    }

    throw new InvalidBooleanVariableValueException();
  }

  public abstract boolean verify();

  public Action getAction() {
    return action;
  }
}
