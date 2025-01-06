package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportedClientDto(String name, List<ExportedFileDto> exportedFileDtos) {
}
