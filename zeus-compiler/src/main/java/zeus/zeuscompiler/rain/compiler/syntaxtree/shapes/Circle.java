package zeus.zeuscompiler.rain.compiler.syntaxtree.shapes;

import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.SortedPosition;
import zeus.zeuscompiler.rain.dtos.ExportShapeDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

public class Circle extends Shape {
  public Circle(
    int line,
    int linePosition,
    String name,
    Position position,
    ShapeProperties shapeProperties,
    boolean blueprint
  ) {
    super(line, linePosition, name, position, shapeProperties, blueprint);
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "<div style={{",
            "%s,",
            "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "}}></div>"
          },
          0
        ),
        this.translateStyle(depth + 1, exportTarget),
        CompilerUtils.buildLinePadding(depth + 2) + String.format(
          "borderRadius: %s",
          Float.parseFloat(this.shapeProperties.properties.get(ShapeProperty.WIDTH)) / 2
        )
      );
    };
  }

  public static Circle fromDto(ExportShapeDto exportShapeDto, boolean blueprint) {
    return new Circle(
      -1,
      -1,
      exportShapeDto.getName(),
      new SortedPosition(exportShapeDto.getPositionX(), exportShapeDto.getPositionY(), exportShapeDto.getSorting()),
      ShapeProperties.fromDtos(exportShapeDto.getExportShapePropertyDtos()),
      blueprint
    );
  }
}
