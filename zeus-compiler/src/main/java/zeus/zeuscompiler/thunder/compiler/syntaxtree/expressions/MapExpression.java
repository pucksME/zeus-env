package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.shared.formula.Formula;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.MapType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MapExpression extends Expression {
  List<MapItem> expressions;

  public MapExpression(int line, int linePosition, List<MapItem> expressions) {
    super(line, linePosition);
    this.expressions = expressions;
  }

  @Override
  public void checkTypes() {
    this.evaluateType();
  }

  @Override
  public Optional<Type> evaluateType() {
    MapType mapType = null;
    for (MapItem mapItem : this.expressions) {
      Optional<Type> keyTypeOptional = mapItem.key.evaluateType();

      if (keyTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      Optional<Type> valueTypeOptional = mapItem.value.evaluateType();

      if (valueTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      MapType currentMapType = new MapType(keyTypeOptional.get(), valueTypeOptional.get());

      if (mapType != null && !currentMapType.equals(mapType)) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          mapItem.key.getLine(),
          mapItem.key.getLinePosition(),
          new IncompatibleTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      mapType = currentMapType;
    }

    return Optional.ofNullable(mapType);
  }

  @Override
  public Formula toFormula() {
    throw new RuntimeException("Could not convert map expression to formula: not implemented yet");
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "new Map([%s])",
        this.expressions.stream().map(mapItem -> String.format(
          "[%s, %s]",
          mapItem.key.translate(depth, exportTarget),
          mapItem.value.translate(depth, exportTarget)
        )).collect(Collectors.joining(", "))
      );
    };
  }
}
