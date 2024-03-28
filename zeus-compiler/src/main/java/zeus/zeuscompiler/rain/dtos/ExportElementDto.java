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
  @JsonSubTypes.Type(value = ExportComponentDto.class, name = "COMPONENT"),
  @JsonSubTypes.Type(value = ExportBlueprintComponentDto.class, name = "BLUEPRINT_COMPONENT"),
  @JsonSubTypes.Type(value = ExportShapeDto.class, name = "SHAPE")
})
public abstract class ExportElementDto {
  String name;
  float positionX;
  float positionY;
  int sorting;
  ExportElementType exportElementType;

  public ExportElementDto(
    String name,
    float positionX,
    float positionY,
    int sorting,
    ExportElementType exportElementType
  ) {
    this.name = name;
    this.positionX = positionX;
    this.positionY = positionY;
    this.sorting = sorting;
    this.exportElementType = exportElementType;
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
