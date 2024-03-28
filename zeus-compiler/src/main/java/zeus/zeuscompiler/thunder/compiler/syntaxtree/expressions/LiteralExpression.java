package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;

import java.util.List;
import java.util.Optional;

public class LiteralExpression extends Expression {
  String value;
  LiteralType type;

  public LiteralExpression(int line, int linePosition, String value, LiteralType type) {
    super(line, linePosition);
    this.value = value;
    this.type = type;
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    return Optional.of(new PrimitiveType(this.type));
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> switch (this.type) {
        case STRING -> String.format("'%s'", this.value);
        case BOOLEAN -> (this.value.equals("true")) ? "true" : "false";
        case INT, FLOAT -> this.value;
        case NULL -> "null";
      };
    };
  }

  public String getValue() {
    return value;
  }
}
