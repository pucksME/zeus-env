package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import java.util.List;

public class Body {
  List<BodyComponent> bodyComponents;

  public Body(List<BodyComponent> bodyComponents) {
    this.bodyComponents = bodyComponents;
  }

  public List<BodyComponent> getBodyComponents() {
    return bodyComponents;
  }
}
