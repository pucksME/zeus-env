package zeus.zeuscompiler.thunder.compiler.utils;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Component;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.ControlStatement;

import java.util.ArrayList;
import java.util.List;

public class ParentStatement {
  ControlStatement controlStatement;
  int index;
  List<BodyComponent> bodyComponents;

  public ParentStatement(ControlStatement controlStatement, List<BodyComponent> bodyComponents, int index) {
    this.controlStatement = controlStatement;
    this.bodyComponents = bodyComponents;
    this.index = index;
  }

  public ControlStatement getControlStatement() {
    return controlStatement;
  }

  public List<Component> getComponents() {
    return new ArrayList<>(this.bodyComponents);
  }

  public int getIndex() {
    return index;
  }
}
