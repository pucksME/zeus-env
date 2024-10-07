package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;

import java.util.Optional;

public class PrimitiveType extends Type {
  PrimitiveTypeType primitiveTypeType;

  public PrimitiveType(PrimitiveTypeType primitiveTypeType) {
    this.primitiveTypeType = primitiveTypeType;
  }

  public static Optional<PrimitiveType> fromThunderType(zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType primitiveType) {
    if (primitiveType.getType() == LiteralType.NULL) {
      return Optional.empty();
    }

    return Optional.of(new PrimitiveType(switch (primitiveType.getType()) {
      case BOOLEAN -> PrimitiveTypeType.BOOLEAN;
      case INT -> PrimitiveTypeType.INT;
      case FLOAT -> PrimitiveTypeType.FLOAT;
      case STRING -> PrimitiveTypeType.STRING;
      default -> throw new RuntimeException("Unsupported primitive thunder type");
    }));
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
