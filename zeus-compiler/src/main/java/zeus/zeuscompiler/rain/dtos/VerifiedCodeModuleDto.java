package zeus.zeuscompiler.rain.dtos;

import zeus.zeuscompiler.thunder.dtos.ErrorDto;

import java.util.List;

public record VerifiedCodeModuleDto(boolean success, List<ErrorDto> errors) {
}
