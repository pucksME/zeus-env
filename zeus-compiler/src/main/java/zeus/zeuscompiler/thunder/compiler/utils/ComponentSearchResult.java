package zeus.zeuscompiler.thunder.compiler.utils;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Component;

import java.util.Queue;

public class ComponentSearchResult {
  Component component;
  int index;
  Queue<ParentStatement> parents;

  public ComponentSearchResult(Component component, int index, Queue<ParentStatement> parents) {
    this.component = component;
    this.index = index;
    this.parents = parents;
  }

  public Component getComponent() {
    return component;
  }

  public int getIndex() {
    return index;
  }

  public Queue<ParentStatement> getParents() {
    return parents;
  }
}
