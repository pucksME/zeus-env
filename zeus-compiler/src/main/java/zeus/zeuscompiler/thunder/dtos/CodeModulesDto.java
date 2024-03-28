package zeus.zeuscompiler.thunder.dtos;

import java.util.List;

public record CodeModulesDto(String uuid, String code, List<CodeModuleDto> codeModules, List<ErrorDto> errors) {
}
