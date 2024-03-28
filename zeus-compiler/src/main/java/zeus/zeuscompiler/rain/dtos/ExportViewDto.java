package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportViewDto(
  String name,
  float height,
  float width,
  float positionX,
  float positionY,
  boolean isRoot,
  List<ExportElementDto> exportElementDtos
) {
}
