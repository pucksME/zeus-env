package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class UmbrellaSpecifications extends Node {
  Map<String, UmbrellaSpecification> umbrellaSpecifications;

  public UmbrellaSpecifications(int line, int linePosition, Map<String, UmbrellaSpecification> umbrellaSpecifications) {
    super(line, linePosition);
    this.umbrellaSpecifications = umbrellaSpecifications;
  }

  @Override
  public void check() {
    this.umbrellaSpecifications.values().forEach(UmbrellaSpecification::check);
  }

  @Override
  public boolean accessesResponse() {
    return this.umbrellaSpecifications.values().stream().anyMatch(UmbrellaSpecification::accessesResponse);
  }

  public HashMap<String, String> translate(String serverName, String routeId) {
    HashMap<String, String> translation = new HashMap<>();

    for (Map.Entry<String, UmbrellaSpecification> idSpecification : this.umbrellaSpecifications.entrySet()) {
      translation.put(idSpecification.getKey(), idSpecification.getValue().translate(serverName, routeId));
    }

    return translation;
  }

  private void initializeUmbrellaSpecification(String id, int line, int linePosition) {
    if (this.umbrellaSpecifications.containsKey(id)) {
      return;
    }

    this.umbrellaSpecifications.put(id, new UmbrellaSpecification(line, linePosition, id));
  }

  public void setUmbrellaSpecificationFormula(String id, int line, int linePosition, Formula formula) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setFormula(formula);
  }

  public void setUmbrellaSpecificationContext(String id, int line, int linePosition, Context context) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setContext(context);
  }

  public void setUmbrellaSpecificationAction(String id, int line, int linePosition, Set<Action> actions) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setActions(actions);
  }

  public Map<String, UmbrellaSpecification> getUmbrellaSpecifications() {
    return umbrellaSpecifications;
  }
}
