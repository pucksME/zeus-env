package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.CompilerError;

import java.util.List;

public class MapType extends Type {
  Type keyType;
  Type valueType;

  public MapType(Type keyType, Type valueType) {
    this.keyType = keyType;
    this.valueType = valueType;
  }

  public MapType(int line, int linePosition, Type keyType, Type valueType) {
    super(line, linePosition);
    this.keyType = keyType;
    this.valueType = valueType;
  }

  @Override
  public void checkType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.keyType.checkType(symbolTable, compilerErrors);
    this.valueType.checkType(symbolTable, compilerErrors);
  }

  @Override
  public boolean compatible(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors, Type type) {
    if (type instanceof MapType) {
      return ((MapType) type).keyType.compatible(symbolTable, compilerErrors, this.keyType) &&
        ((MapType) type).valueType.compatible(symbolTable, compilerErrors, this.valueType);
    }

    return this.equals(type);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof MapType &&
      this.keyType.equals(((MapType) obj).keyType) &&
      this.valueType.equals(((MapType) obj).valueType);
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "Map<%s, %s>",
        this.keyType.translate(symbolTable, depth, exportTarget),
        this.valueType.translate(symbolTable, depth, exportTarget)
      );
    };
  }

  @Override
  public String toString() {
    return "[" + this.keyType.toString() + "," + this.valueType.toString() + "]";
  }

  public Type getKeyType() {
    return keyType;
  }

  public Type getValueType() {
    return valueType;
  }
}
