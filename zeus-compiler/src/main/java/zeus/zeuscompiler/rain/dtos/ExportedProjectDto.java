package zeus.zeuscompiler.rain.dtos;

import zeus.zeuscompiler.thunder.dtos.ErrorDto;

import java.util.List;

public record ExportedProjectDto(
  List<ExportedClientDto> exportedClientDtos,
  ExportedServersDto servers,
  ExportedFileDto umbrellaSpecificationInitialization,
  List<ErrorDto> errors
) {
}
