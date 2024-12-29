package zeus.zeuscompiler.symboltable;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

public class TypeInformation {
  Type type;
  TypeVisibility typeVisibility;

  public TypeInformation(Type type, TypeVisibility typeVisibility) {
    this.type = type;
    this.typeVisibility = typeVisibility;
  }

  public Type getType() {
    return type;
  }

  public TypeVisibility getTypeVisibility() {
    return typeVisibility;
  }
}
