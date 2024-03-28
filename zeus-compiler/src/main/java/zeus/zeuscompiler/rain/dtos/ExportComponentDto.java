package zeus.zeuscompiler.rain.dtos;

import io.swagger.v3.oas.annotations.media.SchemaProperty;

import java.util.List;

public class ExportComponentDto extends ExportElementDto {
  ExportBlueprintComponentReferenceDto exportBlueprintComponentReferenceDto;
  ExportCodeDto exportCodeDto;
  @SchemaProperty()
  List<ExportElementDto> exportElementDtos;

  public ExportComponentDto(
    String name,
    float positionX,
    float positionY,
    int sorting,
    ExportElementType exportElementType,
    ExportBlueprintComponentReferenceDto exportBlueprintComponentReferenceDto,
    ExportCodeDto exportCodeDto,
    List<ExportElementDto> exportElementDtos
  ) {
    super(name, positionX, positionY, sorting, exportElementType);
    this.exportBlueprintComponentReferenceDto = exportBlueprintComponentReferenceDto;
    this.exportCodeDto = exportCodeDto;
    this.exportElementDtos = exportElementDtos;
  }

  public ExportBlueprintComponentReferenceDto getExportBlueprintComponentReferenceDto() {
    return exportBlueprintComponentReferenceDto;
  }

  public ExportCodeDto getExportCodeDto() {
    return exportCodeDto;
  }

  public List<ExportElementDto> getExportElementDtos() {
    return exportElementDtos;
  }
}
