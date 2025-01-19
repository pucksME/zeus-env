package zeus.zeuscompiler.rain.dtos;

import java.util.List;

public record ExportedServersDto(
  ExportedFileDto typingMiddleware,
  List<ExportedServerDto> servers
) {
}
