package zeus.specification;

import com.google.gson.JsonArray;
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
  Map<String, JsonElement> state;
  boolean isFirstRequest;

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
    this.state = new HashMap<>();
    this.isFirstRequest = true;
  }

  private Optional<JsonElement> getVariableValue(String identifier, Map<String, JsonElement> state) {
    return Optional.ofNullable(state.get(identifier));
  }

  private Optional<JsonElement> getVariableValue(String identifier) {
    return this.getVariableValue(identifier, this.state);
  }

  public String getVariableValueAsString(String identifier, Map<String, JsonElement> state) {
    return this.getVariableValue(identifier, state).orElseThrow().getAsString();
  }

  public String getVariableValueAsString(String identifier) {
    return this.getVariableValueAsString(identifier, this.state);
  }

  public int getVariableValueAsInt(String identifier, Map<String, JsonElement> state) {
    return Integer.parseInt(this.getVariableValueAsString(identifier, state));
  }

  public int getVariableValueAsInt(String identifier) {
    return getVariableValueAsInt(identifier, this.state);
  }

  public float getVariableValueAsFloat(String identifier, Map<String, JsonElement> state) {
    return Float.parseFloat(this.getVariableValueAsString(identifier, state));
  }

  public float getVariableValueAsFloat(String identifier) {
    return this.getVariableValueAsFloat(identifier, this.state);
  }

  public boolean getVariableValueAsBoolean(String identifier, Map<String, JsonElement> state) {
    String variableValue = this.getVariableValueAsString(identifier, state);
    if (variableValue.equals("true")) {
      return true;
    }

    if (variableValue.equals("false")) {
      return false;
    }

    throw new InvalidBooleanVariableValueException();
  }

  public boolean getVariableValueAsBoolean(String identifier) {
    return this.getVariableValueAsBoolean(identifier, this.state);
  }

  public List<JsonElement> getVariableValueAsList(String identifier, Map<String, JsonElement> state) {
    return ((JsonArray) this.getVariableValue(identifier, state).orElseThrow()).asList();
  }

  public List<JsonElement> getVariableValueAsList(String identifier) {
    return this.getVariableValueAsList(identifier, this.state);
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
