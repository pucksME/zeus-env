package zeus.zeuscompiler.rain.dtos;

import io.swagger.v3.oas.annotations.media.SchemaProperty;

import java.util.List;

public class ExportBlueprintComponentDto extends ExportElementDto {
  // https://www.baeldung.com/java-swagger-set-list-response#2-modifying-swagger-api-response [accessed 6/9/2023, 13:01]
  // @ArraySchema(schema = @Schema(implementation = ExportElementDto.class))
  @SchemaProperty()
  List<ExportElementDto> exportElementDtos;

  public ExportBlueprintComponentDto(
    String name,
    float positionX,
    float positionY,
    int sorting,
    ExportElementType exportElementType,
    List<ExportElementDto> exportElementDtos
  ) {
    super(name, positionX, positionY, sorting, exportElementType);
    this.exportElementDtos = exportElementDtos;
  }

  public List<ExportElementDto> getExportElementDtos() {
    return exportElementDtos;
  }
}
