package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types;

public class PrimitiveType extends Type {
  PrimitiveTypeType primitiveTypeType;

  public PrimitiveType(PrimitiveTypeType primitiveTypeType) {
    this.primitiveTypeType = primitiveTypeType;
  }

  public PrimitiveTypeType getType() {
    return this.primitiveTypeType;
  }

  @Override
  public boolean compatible(Type type) {
    if (!(type instanceof PrimitiveType)) {
      return false;
    }

    if ((this.primitiveTypeType == PrimitiveTypeType.INT && ((PrimitiveType) type).primitiveTypeType == PrimitiveTypeType.FLOAT) ||
      (this.primitiveTypeType == PrimitiveTypeType.FLOAT && ((PrimitiveType) type).primitiveTypeType == PrimitiveTypeType.INT)) {
      return true;
    }

    return this.equals(type);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PrimitiveType && ((PrimitiveType) obj).primitiveTypeType == this.primitiveTypeType;
  }
}
