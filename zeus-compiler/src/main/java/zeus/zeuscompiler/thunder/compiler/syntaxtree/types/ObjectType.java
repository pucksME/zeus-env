package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.rain.dtos.ExportTarget;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ObjectType extends Type {
  Map<String, Type> propertyTypes;

  public ObjectType(Map<String, Type> propertyTypes) {
    this.propertyTypes = propertyTypes;
  }

  public ObjectType(int line, int linePosition, Map<String, Type> propertyTypes) {
    super(line, linePosition);
    this.propertyTypes = propertyTypes;
  }

  @Override
  public void checkType() {
    for (Type propertyType : this.propertyTypes.values()) {
      propertyType.checkType();
    }
  }

  @Override
  public boolean compatible(Type type) {
    if (type instanceof IdType) {
      return type.compatible(this);
    }

    return this.equals(type);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ObjectType) || ((ObjectType) obj).propertyTypes.size() != this.propertyTypes.size()) {
      return false;
    }

    for (Map.Entry<String, Type> propertyType : this.propertyTypes.entrySet()) {
      if (!((ObjectType) obj).propertyTypes.containsKey(propertyType.getKey()) ||
        !((ObjectType) obj).propertyTypes.get(propertyType.getKey()).equals(propertyType.getValue())) {
        return false;
      }
    }

    return true;
  }

  public String translateToUrlParameters(ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.propertyTypes.keySet().stream()
        .map(key -> String.format("%s/:%s", key, key))
        .collect(Collectors.joining("/"));
    };
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "{%s}",
        this.propertyTypes.entrySet().stream().map(property -> String.format(
          "%s: %s",
          property.getKey(),
          property.getValue().translate(depth, exportTarget)
        )).collect(Collectors.joining(", "))
      );
    };
  }

  @Override
  public String toString() {
    return "{" + this.propertyTypes.entrySet().stream().map(
      propertyType -> propertyType.getKey() + ":" + propertyType.getValue().toString()
    ).collect(Collectors.joining(",")) + "}";
  }

  public Optional<Type> getPropertyType(String propertyId) {
    Type propertyType = this.propertyTypes.get(propertyId);
    return (propertyType == null) ? Optional.empty() : Optional.of(propertyType);
  }

  @Override
  public Optional<Type> getType(List<String> identifiers) {
    if (identifiers.isEmpty()) {
      return Optional.of(this);
    }

    Optional<Type> typeOptional = this.getPropertyType(identifiers.get(0));
    if (typeOptional.isEmpty()) {
      return Optional.empty();
    }

    if (identifiers.size() > 1) {
      Type type = typeOptional.get();
      return type.getType(identifiers.subList(1, identifiers.size()));
    }

    return typeOptional;
  }
}
