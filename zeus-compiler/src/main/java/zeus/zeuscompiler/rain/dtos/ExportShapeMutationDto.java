package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportShapeMutationDto(
  String shapeName,
  Float positionX,
  Float positionY,
  List<ExportShapePropertyDto> exportShapePropertyDtos
) {
}
