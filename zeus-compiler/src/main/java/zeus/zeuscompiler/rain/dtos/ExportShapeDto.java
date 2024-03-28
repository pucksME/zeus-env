package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public class ExportShapeDto extends ExportElementDto {
  ExportShapeType exportShapeType;
  List<ExportShapePropertyDto> exportShapePropertyDtos;

  public ExportShapeDto(
    String name,
    float positionX,
    float positionY,
    int sorting,
    ExportElementType exportElementType,
    ExportShapeType exportShapeType,
    List<ExportShapePropertyDto> exportShapePropertyDtos
  ) {
    super(name, positionX, positionY, sorting, exportElementType);
    this.exportShapeType = exportShapeType;
    this.exportShapePropertyDtos = exportShapePropertyDtos;
  }

  public ExportShapeType getExportShapeType() {
    return exportShapeType;
  }

  public List<ExportShapePropertyDto> getExportShapePropertyDtos() {
    return exportShapePropertyDtos;
  }
}
