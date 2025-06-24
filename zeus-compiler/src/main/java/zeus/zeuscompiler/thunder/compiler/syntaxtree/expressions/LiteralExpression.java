package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.shared.formula.*;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

import java.util.Map;
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
  public Formula toFormula(Map<String, VariableInformation> variables) {
    return switch (this.type) {
      case STRING -> new StringLiteralFormula(this.getValue());
      case FLOAT -> new FloatLiteralFormula(Float.parseFloat(this.getValue()));
      case BOOLEAN -> new BooleanLiteralFormula(this.getValueAsBoolean());
      case INT -> new IntegerLiteralFormula(Integer.parseInt(this.getValue()));
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
