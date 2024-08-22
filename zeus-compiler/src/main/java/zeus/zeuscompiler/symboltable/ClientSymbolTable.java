package zeus.zeuscompiler.symboltable;

import zeus.zeuscompiler.rain.compiler.syntaxtree.BlueprintComponent;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Component;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Element;
import zeus.zeuscompiler.rain.compiler.syntaxtree.View;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.Shape;

import java.util.*;

public class ClientSymbolTable extends SymbolTable {
  Element currentComponent;
  Set<String> currentComponentNames;
  Set<String> currentComponentShapeNames;
  Map<String, BlueprintComponent> blueprintComponents;
  Map<String, View> views;


  public ClientSymbolTable() {
    this.blueprintComponents = new HashMap<>();
    this.views = new HashMap<>();
  }

  public void setCurrentComponent(Element currentComponent) {
    assert currentComponent instanceof BlueprintComponent || currentComponent instanceof Component;
    this.currentComponent = currentComponent;
    this.currentComponentNames = new HashSet<>();
    this.currentComponentShapeNames = new HashSet<>();
  }

  public boolean addCurrentComponentName(Element component) {
    assert component instanceof BlueprintComponent || component instanceof Component;
    return !this.currentComponentNames.add(component.getName());
  }

  public boolean addCurrentComponentShapeName(Shape shape) {
    return !this.currentComponentShapeNames.add(shape.getName());
  }

  public boolean addBlueprintComponent(BlueprintComponent blueprintComponent) {
    boolean exists = this.blueprintComponents.containsKey(blueprintComponent.getName());
    this.blueprintComponents.put(blueprintComponent.getName(), blueprintComponent);
    return exists;
  }

  public boolean containsBlueprintComponent(String id) {
    return this.blueprintComponents.containsKey(id);
  }

  public boolean addView(View view) {
    boolean exists = this.views.containsKey(view.getName());
    this.views.put(view.getName(), view);
    return exists;
  }
}
