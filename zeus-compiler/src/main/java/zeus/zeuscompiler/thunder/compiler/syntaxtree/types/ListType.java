package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
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
  public void checkType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.type.checkType(symbolTable, compilerErrors);
  }

  @Override
  public boolean compatible(SymbolTable symbolTable, List<CompilerError> compilerErrors, Type type) {
    if (type instanceof ListType && ((ListType) type).type instanceof IdType) {
      return ((ListType) type).type.compatible(symbolTable, compilerErrors, this.type);
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
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.type.translate(symbolTable, depth, exportTarget) + "[]";
    };
  }

  public Type getType() {
    return type;
  }
}
