package zeus.specification;

import com.google.gson.JsonElement;
import zeus.Request;

import java.util.*;
import java.util.stream.Collectors;

public abstract class Specification {
  String serverName;
  String routeId;
  String name;
  Context context;
  Set<Action> actions;
  boolean accessesResponse;
  List<Request> requests;
  Map<String, JsonElement> state;

  public Specification(
    String serverName,
    String routeId,
    String name,
    Context context,
    Set<Action> actions,
    boolean accessesResponse
  ) {
    this.serverName = serverName;
    this.routeId = routeId;
    this.name = name;
    this.context = context;
    this.actions = actions;
    this.accessesResponse = accessesResponse;
    this.requests = new ArrayList<>();
    this.state = new HashMap<>();
  }

  private Optional<JsonElement> getVariableValue(String identifier) {
    return Optional.ofNullable(this.state.get(identifier));
  }

  public String getVariableValueAsString(String identifier) {
    return this.getVariableValue(identifier).orElseThrow().getAsString();
  }

  public int getVariableValueAsInt(String identifier) {
    return Integer.parseInt(this.getVariableValueAsString(identifier));
  }

  public float getVariableValueAsFloat(String identifier) {
    return Float.parseFloat(this.getVariableValueAsString(identifier));
  }

  public boolean getVariableValueAsBoolean(String identifier) {
    String variableValue = this.getVariableValueAsString(identifier);
    if (variableValue.equals("true")) {
      return true;
    }

    if (variableValue.equals("false")) {
      return false;
    }

    throw new InvalidBooleanVariableValueException();
  }

  public abstract boolean verify(Request request);

  public String actionsToString() {
    return this.actions.stream()
      .map(action -> switch (action) {
        case BLOCK -> "block";
        case LOG -> "log";
      })
      .collect(Collectors.joining(", "));
  }
  public Set<Action> getActions() {
    return actions;
  }

  public boolean accessesResponse() {
    return accessesResponse;
  }

  public String getId() {
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
