package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportedServerDto(String name, List<ExportedFileDto> exportedFileDtos, List<ExportedFileDto> exportedBootsMonitorFilesDto) {
}
