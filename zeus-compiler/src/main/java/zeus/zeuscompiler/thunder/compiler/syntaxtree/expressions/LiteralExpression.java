package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
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
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    return Optional.of(new PrimitiveType(this.type));
  }

  @Override
  public Expr toFormula(Context context) {
    return switch (this.type) {
      case STRING -> context.mkString(this.getValue());
      case FLOAT -> context.mkReal(this.getValue());
      case BOOLEAN -> context.mkBool(this.getValueAsBoolean());
      case INT -> context.mkInt(this.getValue());
      default -> throw new RuntimeException(String.format(
        "Could not convert literal expression to formula: type \"%s\" is not supported",
        this.type
      ));
    };
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> switch (this.type) {
        case STRING -> String.format("'%s'", this.value);
        case BOOLEAN -> this.getValueAsBoolean() ? "true" : "false";
        case INT, FLOAT -> this.value;
        case NULL -> "null";
      };
    };
  }

  public String getValue() {
    return value;
  }

  public boolean getValueAsBoolean() {
    return this.value.equals("true");
  }
}
