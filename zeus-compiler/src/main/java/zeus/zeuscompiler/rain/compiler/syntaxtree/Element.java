package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.Shape;
import zeus.zeuscompiler.rain.dtos.ExportBlueprintComponentDto;
import zeus.zeuscompiler.rain.dtos.ExportComponentDto;
import zeus.zeuscompiler.rain.dtos.ExportShapeDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;

public abstract class Element extends Node {
  Position position;

  public Element(int line, int linePosition, String name, Position position) {
    super(line, linePosition, name);
    this.position = position;
  }

  public abstract String translateReference(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget);

  public static Element fromDto(Object dto, boolean blueprint) {
    if (dto instanceof ExportBlueprintComponentDto) {
      return BlueprintComponent.fromDto((ExportBlueprintComponentDto) dto);
    }

    if (dto instanceof ExportComponentDto) {
      return Component.fromDto((ExportComponentDto) dto);
    }

    if (dto instanceof ExportShapeDto) {
      return Shape.fromDto((ExportShapeDto) dto, blueprint);
    }

    return null;
  }

  public Position getPosition() {
    return position;
  }
}
