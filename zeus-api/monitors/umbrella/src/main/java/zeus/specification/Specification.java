package zeus.specification;

import zeus.Request;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class Specification {
  String serverName;
  String routeId;
  String name;
  Context context;
  Action action;
  boolean accessesResponse;
  List<Request> requests;

  public Specification(
    String serverName,
    String routeId,
    String name,
    Context context,
    Action action,
    boolean accessesResponse
  ) {
    this.serverName = serverName;
    this.routeId = routeId;
    this.name = name;
    this.context = context;
    this.action = action;
    this.accessesResponse = accessesResponse;
    this.requests = new ArrayList<>();
  }

  private Optional<String> getVariableValue(String identifier, Map<String, String> variables) {
    return Optional.ofNullable(variables.get(identifier));
  }

  public String getVariableValueAsString(String identifier, Map<String, String> variables) {
    return this.getVariableValue(identifier, variables).orElseThrow();
  }

  public int getVariableValueAsInt(String identifier, Map<String, String> variables) {
    return Integer.parseInt(this.getVariableValue(identifier, variables).orElseThrow());
  }

  public float getVariableValueAsFloat(String identifier, Map<String, String> variables) {
    return Float.parseFloat(this.getVariableValue(identifier, variables).orElseThrow());
  }

  public boolean getVariableValueAsBoolean(String identifier, Map<String, String> variables) {
    String variableValue = this.getVariableValue(identifier, variables).orElseThrow();
    if (variableValue.equals("true")) {
      return true;
    }

    if (variableValue.equals("false")) {
      return false;
    }

    throw new InvalidBooleanVariableValueException();
  }

  public abstract boolean verify(Request request);

  public Action getAction() {
    return action;
  }

  public boolean accessesResponse() {
    return accessesResponse;
  }

  private String getId() {
    return String.format("%s/%s/%s", serverName, routeId, name);
  }

  @Override
  public int hashCode() {
    return this.getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Specification)) {
      return false;
    }

    return this.getId().equals(((Specification) obj).getId());
  }
}
