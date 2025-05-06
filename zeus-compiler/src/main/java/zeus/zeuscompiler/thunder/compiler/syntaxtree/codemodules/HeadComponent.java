package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

public abstract class HeadComponent extends Component {
  String id;
  Type type;

  public HeadComponent(int line, int linePosition, String id, Type type) {
    super(line, linePosition);
    this.id = id;
    this.type = type;
  }

  public String getId() {
    return id;
  }

  public Type getType() {
    return type;
  }
}
