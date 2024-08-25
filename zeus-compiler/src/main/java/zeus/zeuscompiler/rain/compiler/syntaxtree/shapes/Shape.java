package zeus.zeuscompiler.rain.compiler.syntaxtree.shapes;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Element;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementType;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.IncompatibleShapePropertyException;
import zeus.zeuscompiler.rain.dtos.ExportShapeDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Shape extends Element {
  ShapeProperties shapeProperties;
  Set<ShapeProperty> compatibleShapeProperties;
  boolean blueprint;

  public Shape(
    int line,
    int linePosition,
    String name,
    Position position,
    ShapeProperties shapeProperties,
    boolean blueprint
  ) {
    super(line, linePosition, name, position);
    this.shapeProperties = shapeProperties;
    this.blueprint = blueprint;
    this.compatibleShapeProperties = new HashSet<>(Set.of(
      ShapeProperty.HEIGHT,
      ShapeProperty.WIDTH,
      ShapeProperty.BACKGROUND_COLOR_ENABLED,
      ShapeProperty.BACKGROUND_COLOR,
      ShapeProperty.BORDER_ENABLED,
      ShapeProperty.BORDER_COLOR,
      ShapeProperty.BORDER_WIDTH,
      ShapeProperty.SHADOW_ENABLED,
      ShapeProperty.SHADOW_COLOR,
      ShapeProperty.SHADOW_X,
      ShapeProperty.SHADOW_Y,
      ShapeProperty.SHADOW_BLUR,
      ShapeProperty.OPACITY,
      ShapeProperty.VISIBLE
    ));
  }

  public void checkShapeProperties() {
    if (!this.compatibleShapeProperties.containsAll(this.shapeProperties.properties.keySet())) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleShapePropertyException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public void check() {
    if (ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ClientSymbolTable.class).addCurrentComponentShapeName(this)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new AmbiguousElementException(this.getName(), AmbiguousElementType.SHAPE),
        CompilerPhase.TYPE_CHECKER
      ));
    }
    this.checkShapeProperties();
  }

  String translateStyle(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        String linePadding = CompilerUtils.buildLinePadding(depth + 1);
        yield String.format(
          "%s%s%s",
          linePadding + this.getPosition().translate(depth + 1, exportTarget),
          (!this.shapeProperties.properties.isEmpty())
            ? String.format(",\n%s", this.shapeProperties.translate(depth + 1, exportTarget))
            : "",
          (this.blueprint)
            ? String.format(
              ",\n%s...((properties.mutations && properties.mutations.%s) ? properties.mutations.%s.style : undefined)",
              linePadding,
              this.getName(),
              this.getName()
            )
            : ""
        );
      }
    };
  }

  @Override
  public String translateReference(int depth, ExportTarget exportTarget) {
    return "";
  }

  public static Shape fromDto(ExportShapeDto exportShapeDto, boolean blueprint) {
    return switch (exportShapeDto.getExportShapeType()) {
      case RECTANGLE -> Rectangle.fromDto(exportShapeDto, blueprint);
      case CIRCLE -> Circle.fromDto(exportShapeDto, blueprint);
      case TEXT -> Text.fromDto(exportShapeDto, blueprint);
    };
  }
}
