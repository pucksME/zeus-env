package zeus.zeuscompiler.rain.dtos;

import zeus.zeuscompiler.thunder.dtos.ErrorDto;

import java.util.List;

public record ExportedProjectDto(List<ExportedFileDto> exportedFileDtos, List<ErrorDto> errors) {
}
