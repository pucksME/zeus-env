package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.rain.dtos.ExportTarget;

import java.util.Map;

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
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
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

  public void setUmbrellaSpecificationAction(String id, int line, int linePosition, Action action) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setAction(action);
  }
}
