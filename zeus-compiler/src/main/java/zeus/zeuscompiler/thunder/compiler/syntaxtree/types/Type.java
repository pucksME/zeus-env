package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Node;
import zeus.zeuscompiler.thunder.dtos.TypeDto;

import java.util.List;
import java.util.Optional;

public abstract class Type extends Node implements Convertable<TypeDto>, Translatable {
  public Type() {
    super(-1, -1);
  }

  public Type(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract void checkType();

  public abstract boolean compatible(Type type);

  public abstract Optional<Type> getType(List<String> identifiers);

  @Override
  public TypeDto toDto() {
    return new TypeDto(this.toString());
  }
}
