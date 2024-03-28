package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.UnknownPrimitiveTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.CompilerError;

import java.util.List;

public class PrimitiveType extends Type {
  LiteralType type;

  public PrimitiveType(LiteralType type) {
    super(-1, -1);
    this.type = type;
  }

  public PrimitiveType(int line, int linePosition, String typeName) {
    super(line, linePosition);
    switch (typeName) {
      case "int":
        this.type = LiteralType.INT;
        break;
      case "float":
        this.type = LiteralType.FLOAT;
        break;
      case "string":
        this.type = LiteralType.STRING;
        break;
      case "boolean":
        this.type = LiteralType.BOOLEAN;
        break;
      default:
        throw new UnknownPrimitiveTypeException();
    }
  }

  @Override
  public void checkType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
  }

  @Override
  public boolean compatible(SymbolTable symbolTable, List<CompilerError> compilerErrors, Type type) {
    if (!(type instanceof PrimitiveType)) {
      return false;
    }

    if ((((PrimitiveType) type).type == LiteralType.INT && this.type == LiteralType.FLOAT) ||
      (((PrimitiveType) type).type == LiteralType.FLOAT && this.type == LiteralType.INT)) {
      return true;
    }

    return this.equals(type);
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof PrimitiveType && ((PrimitiveType) obj).type.equals(this.type);
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> switch (this.type) {
        case STRING -> "string";
        case BOOLEAN -> "boolean";
        case INT, FLOAT -> "number";
        case NULL -> "null";
      };
    };
  }

  @Override
  public String toString() {
    // https://docs.oracle.com/en/java/javase/13/language/switch-expressions.html [accessed 26/4/2023, 17:35]
    return switch (this.type) {
      case INT -> "int";
      case FLOAT -> "float";
      case STRING -> "string";
      case BOOLEAN -> "boolean";
      default -> throw new UnknownPrimitiveTypeException();
    };
  }

  public LiteralType getType() {
    return type;
  }
}
