package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportBlueprintComponentReferenceDto(
  String blueprintComponentName,
  List<ExportComponentMutationDto> exportComponentMutationDtos,
  List<ExportShapeMutationDto> exportShapeMutationDtos
) {
}
