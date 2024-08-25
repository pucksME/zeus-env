package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.CompilerError;

import java.util.List;

public class ListType extends Type {
  Type type;
  // defines the list's size, -1 if size is not restricted
  int size;

  public ListType(Type type, int size) {
    this.type = type;
    this.size = size;
  }

  public ListType(int line, int linePosition, Type type, int size) {
    super(line, linePosition);
    this.type = type;
    this.size = size;
  }

  @Override
  public void checkType() {
    this.type.checkType();
  }

  @Override
  public boolean compatible(Type type) {
    if (type instanceof ListType && ((ListType) type).type instanceof IdType) {
      return ((ListType) type).type.compatible(this.type);
    }

    return this.equals(type);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof ListType && this.type.equals(((ListType) obj).type);
  }

  @Override
  public String toString() {
    String type = "[" + this.type.toString() + "]";
    return (this.size != -1) ? type + "#" + this.size : type;
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.type.translate(depth, exportTarget) + "[]";
    };
  }

  public Type getType() {
    return type;
  }
}
