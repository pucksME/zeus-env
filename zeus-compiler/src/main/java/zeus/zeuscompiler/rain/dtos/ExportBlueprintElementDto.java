package zeus.zeuscompiler.rain.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "exportElementType",
  use = JsonTypeInfo.Id.NAME,
  visible = true
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ExportBlueprintComponentDto.class, name = "COMPONENT"),
  @JsonSubTypes.Type(value = ExportBlueprintShapeDto.class, name = "SHAPE")
})
public class ExportBlueprintElementDto {
  String uuid;
  String name;
  float positionX;
  float positionY;
  int sorting;
  ExportElementType exportElementType;

  public ExportBlueprintElementDto(
    String uuid,
    String name,
    float positionX,
    float positionY,
    int sorting,
    ExportElementType exportElementType
  ) {
    this.uuid = uuid;
    this.name = name;
    this.positionX = positionX;
    this.positionY = positionY;
    this.sorting = sorting;
    this.exportElementType = exportElementType;
  }

  public String getUuid() {
    return uuid;
  }

  public String getName() {
    return name;
  }

  public float getPositionX() {
    return positionX;
  }

  public float getPositionY() {
    return positionY;
  }

  public int getSorting() {
    return sorting;
  }

  public ExportElementType getExportElementType() {
    return exportElementType;
  }
}
