package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
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
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.evaluateType(symbolTable, compilerErrors);
  }

  @Override
  public Optional<Type> evaluateType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    MapType mapType = null;
    for (MapItem mapItem : this.expressions) {
      Optional<Type> keyTypeOptional = mapItem.key.evaluateType(symbolTable, compilerErrors);

      if (keyTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      Optional<Type> valueTypeOptional = mapItem.value.evaluateType(symbolTable, compilerErrors);

      if (valueTypeOptional.isEmpty()) {
        return Optional.empty();
      }

      MapType currentMapType = new MapType(keyTypeOptional.get(), valueTypeOptional.get());

      if (mapType != null && !currentMapType.equals(mapType)) {
        compilerErrors.add(new CompilerError(
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
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "new Map([%s])",
        this.expressions.stream().map(mapItem -> String.format(
          "[%s, %s]",
          mapItem.key.translate(symbolTable, depth, exportTarget),
          mapItem.value.translate(symbolTable, depth, exportTarget)
        )).collect(Collectors.joining(", "))
      );
    };
  }
}
