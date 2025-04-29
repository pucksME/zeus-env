package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ObjectExpression extends Expression {
  List<ObjectItem> properties;

  public ObjectExpression(int line, int linePosition, List<ObjectItem> properties) {
    super(line, linePosition);
    this.properties = properties;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    Map<String, Type> propertyTypes = new HashMap<>();

    for (ObjectItem property : this.properties) {
      Optional<Type> propertyTypeOptional = property.expression.evaluateType();

      if (propertyTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      propertyTypes.put(property.id, propertyTypeOptional.get());
    }

    return Optional.of(new ObjectType(propertyTypes));
  }

  @Override
  public Expr toFormula(Context context) {
    throw new RuntimeException("Could not convert object expression to formula: not implemented yet");
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "{%s}",
        this.properties.stream().map(objectItem -> String.format(
          "%s: %s",
          objectItem.id,
          objectItem.expression.translate(depth, exportTarget)
        )).collect(Collectors.joining(", "))
      );
    };
  }
}
