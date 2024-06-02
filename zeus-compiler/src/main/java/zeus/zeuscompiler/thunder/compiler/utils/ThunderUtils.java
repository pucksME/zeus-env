package zeus.zeuscompiler.thunder.compiler.utils;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.CodeModuleComponent;
import zeus.zeuscompiler.thunder.dtos.*;

import java.util.List;
import java.util.stream.Collectors;

public abstract class ThunderUtils {
  public static CodeModulesDto buildCodeModulesDto(String uuid, String code, CodeModules codeModules, List<CompilerError> compilerErrors) {
    return new CodeModulesDto(uuid, code, codeModules.toDto(), compilerErrors.stream().map(CompilerError::toDto).toList());
  }

  public static CharStream buildCharStream(List<CreateCodeModuleDto> createCodeModuleDtos) {
    // https://stackoverflow.com/a/10937340 [accessed 25/4/2023, 13:39]
    // https://stackoverflow.com/a/37987434 [accessed 25/4/2023, 13:43]
    return CharStreams.fromString(createCodeModuleDtos.stream().map(CreateCodeModuleDto::code).collect(
      Collectors.joining(System.lineSeparator())
    ));
  }

  public static String buildErrorMessage(String description, int line, int linePosition) {
    return description + " at [" + line + "," + linePosition + "]";
  }

  public static String codeModuleComponentToString(CodeModuleComponent codeModuleComponent) {
    return switch (codeModuleComponent) {
      case INPUT -> "input";
      case OUTPUT -> "output";
      case CONFIG -> "config";
      case BODY -> "body";
    };
  }
}
