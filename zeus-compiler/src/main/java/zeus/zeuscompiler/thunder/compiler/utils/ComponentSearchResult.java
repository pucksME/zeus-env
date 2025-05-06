package zeus.zeuscompiler.thunder.compiler.utils;

import java.util.Queue;

public class ComponentSearchResult {
  int index;
  Queue<ParentStatement> parents;

  public ComponentSearchResult(int index, Queue<ParentStatement> parents) {
    this.index = index;
    this.parents = parents;
  }

  public int getIndex() {
    return index;
  }

  public Queue<ParentStatement> getParents() {
    return parents;
  }
}
