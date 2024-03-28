package zeus.zeuscompiler.thunder.compiler.symboltable;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;

public class TypeInformation {
  ObjectType type;
  TypeVisibility typeVisibility;

  public TypeInformation(ObjectType type, TypeVisibility typeVisibility) {
    this.type = type;
    this.typeVisibility = typeVisibility;
  }

  public ObjectType getType() {
    return type;
  }

  public TypeVisibility getTypeVisibility() {
    return typeVisibility;
  }
}
