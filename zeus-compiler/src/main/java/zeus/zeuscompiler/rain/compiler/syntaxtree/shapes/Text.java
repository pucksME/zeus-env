package zeus.zeuscompiler.rain.compiler.syntaxtree.shapes;

import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.SortedPosition;
import zeus.zeuscompiler.rain.dtos.ExportShapeDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.Set;

public class Text extends Shape {
  public Text(
    int line,
    int linePosition,
    String name,
    Position position,
    ShapeProperties shapeProperties,
    boolean blueprint
  ) {
    super(line, linePosition, name, position, shapeProperties, blueprint);
    this.compatibleShapeProperties.addAll(Set.of(
      ShapeProperty.FONT_FAMILY,
      ShapeProperty.FONT_SIZE,
      ShapeProperty.FONT_STYLE,
      ShapeProperty.TEXT,
      ShapeProperty.TEXT_DECORATION,
      ShapeProperty.TEXT_TRANSFORM,
      ShapeProperty.TEXT_ALIGN,
      ShapeProperty.TEXT_COLOR
    ));

    if (shapeProperties.properties.containsKey(ShapeProperty.TEXT)) {
      return;
    }

    String backgroundColor = shapeProperties.properties.get(ShapeProperty.BACKGROUND_COLOR);
    if (backgroundColor != null) {
      shapeProperties.properties.put(ShapeProperty.TEXT_COLOR, backgroundColor);
      shapeProperties.properties.remove(ShapeProperty.BACKGROUND_COLOR);
    }
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "<p style={{",
            "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "}}>",
            CompilerUtils.buildLinePadding(depth + 2) + "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "</p>"
          },
          0
        ),
        this.translateStyle(symbolTable, depth + 1, exportTarget),
        (this.blueprint)
          ? String.format(
            CompilerUtils.buildLinesFormat(
              new String[]{
                "{(properties.mutations && properties.mutations.%s && properties.mutations.%s.data && properties.mutations.%s.data.text)",
                CompilerUtils.buildLinePadding(depth + 3) + "? %s",
                CompilerUtils.buildLinePadding(depth + 3) + ": '%s'}"
              },
              0
            ),
            this.getName(),
            this.getName(),
            this.getName(),
            String.format("properties.mutations.%s.data.text", this.getName()),
            this.shapeProperties.properties.get(ShapeProperty.TEXT)
          )
          : this.shapeProperties.properties.get(ShapeProperty.TEXT)
      );
    };
  }

  public static Text fromDto(ExportShapeDto exportShapeDto, boolean blueprint) {
    return new Text(
      -1,
      -1,
      exportShapeDto.getName(),
      new SortedPosition(exportShapeDto.getPositionX(), exportShapeDto.getPositionY(), exportShapeDto.getSorting()),
      ShapeProperties.fromDtos(exportShapeDto.getExportShapePropertyDtos()),
      blueprint
    );
  }
}
