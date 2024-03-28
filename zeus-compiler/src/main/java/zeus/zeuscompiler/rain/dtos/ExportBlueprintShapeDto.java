package zeus.zeuscompiler.rain.dtos;

public class ExportBlueprintShapeDto extends ExportBlueprintElementDto {
  public ExportBlueprintShapeDto(
    String uuid,
    String name,
    float positionX,
    float positionY,
    int sorting,
    ExportElementType exportElementType
  ) {
    super(uuid, name, positionX, positionY, sorting, exportElementType);
  }
}
