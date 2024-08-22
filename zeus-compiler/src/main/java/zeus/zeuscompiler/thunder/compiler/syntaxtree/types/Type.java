package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Node;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.dtos.TypeDto;

import java.util.List;

public abstract class Type extends Node implements Convertable<TypeDto>, Translatable {
  public Type() {
    super(-1, -1);
  }

  public Type(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract void checkType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors);

  public abstract boolean compatible(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors, Type type);

  @Override
  public TypeDto toDto() {
    return new TypeDto(this.toString());
  }
}
