package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportProjectDto(
  String name,
  List<ExportElementDto> exportElementDtos,
  List<ExportViewDto> exportViewDtos,
  ExportTarget exportTarget
) {
}
