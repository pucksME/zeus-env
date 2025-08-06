package zeus.zeuscompiler.rain.dtos;

import zeus.zeuscompiler.thunder.dtos.CodeModuleCounterexampleDto;
import zeus.zeuscompiler.thunder.dtos.ErrorDto;

import java.util.List;
import java.util.Set;

public record VerifiedCodeModuleDto(Set<CodeModuleCounterexampleDto> counterexamples, List<ErrorDto> errors) {
}
