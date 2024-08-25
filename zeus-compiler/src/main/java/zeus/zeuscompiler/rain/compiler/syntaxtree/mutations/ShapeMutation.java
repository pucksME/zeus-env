package zeus.zeuscompiler.rain.compiler.syntaxtree.mutations;

import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.ShapeProperties;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.ShapeProperty;
import zeus.zeuscompiler.rain.dtos.ExportShapeMutationDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

public class ShapeMutation extends Mutation {
  String shapeName;
  ShapeProperties shapeProperties;

  public ShapeMutation(Position position, String shapeName, ShapeProperties shapeProperties) {
    super(position);
    this.shapeName = shapeName;
    this.shapeProperties = shapeProperties;
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        String translatedPosition = this.position.translate(depth + 2, exportTarget);
        String translatedMutationStyle = String.format(
          "%s%s%s",
          (!translatedPosition.isEmpty()) ? String.format("%s", translatedPosition) : "",
          (!translatedPosition.isEmpty()) ? ",\n" : "",
          this.shapeProperties.translate(depth + 2, exportTarget)
        );

        String text = this.shapeProperties.getProperties().get(ShapeProperty.TEXT);
        String translatedMutationData = (text != null)
          ? String.format(
              CompilerUtils.buildLinesFormat(
                new String[]{
                  ",",
                  CompilerUtils.buildLinesFormat(
                    new String[]{
                      "data: {",
                      CompilerUtils.buildLinePadding(1) + "text: '%s'",
                      "}"
                    },
                    depth + 1
                  )
                },
                0
              ),
            text
          )
          : "";

        translatedMutationStyle = (!translatedMutationStyle.isEmpty())
          ? String.format(
            CompilerUtils.buildLinesFormat(
              new String[]{
                CompilerUtils.buildLinePadding(depth + 1) + "style: {",
                CompilerUtils.buildLinePadding((!translatedPosition.isEmpty()) ? depth + 2 : 0) + "%s",
                CompilerUtils.buildLinePadding(depth + 1) + "}"
              },
              0
            ),
            translatedMutationStyle
          )
          : "";

        yield (!(translatedMutationStyle + translatedMutationData).isEmpty())
          ? String.format(
            CompilerUtils.buildLinesFormat(
              new String[]{
                "%s: {",
                "%s%s",
                CompilerUtils.buildLinePadding(depth) + "}"
              },
              0
            ),
            this.shapeName,
            translatedMutationStyle,
            translatedMutationData
          )
          : "";
      }
    };
  }

  public static ShapeMutation fromDto(ExportShapeMutationDto exportShapeMutationDto) {
    return new ShapeMutation(
      new Position(exportShapeMutationDto.positionX(), exportShapeMutationDto.positionY()),
      exportShapeMutationDto.shapeName(),
      ShapeProperties.fromDtos(exportShapeMutationDto.exportShapePropertyDtos())
    );
  }
}
