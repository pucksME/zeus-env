package zeus.zeuscompiler;

import org.antlr.v4.runtime.CharStreams;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import zeus.zeuscompiler.rain.compiler.RainAnalyzer;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Project;
import zeus.zeuscompiler.rain.dtos.ExportProjectDto;
import zeus.zeuscompiler.rain.dtos.ExportedFileDto;
import zeus.zeuscompiler.rain.dtos.ExportedProjectDto;
import zeus.zeuscompiler.rain.dtos.TranslateProjectDto;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzerMode;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.thunder.dtos.CodeModulesDto;
import zeus.zeuscompiler.thunder.dtos.CreateCodeModuleDto;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.utils.ThunderUtils;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
public class ZeusCompilerApplication {

  public static void main(String[] args) {
    SpringApplication.run(ZeusCompilerApplication.class, args);
  }

  @Operation(
    summary = "Creates a structured code module from its code",
    description = "Creates a structured code module from its code by letting the thunder compiler process it"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "The structured code module was created successfully"
    )
  })
  @PostMapping("/createCodeModules")
  List<CodeModulesDto> createCodeModules(@RequestBody List<CreateCodeModuleDto> createCodeModuleDtos) {
    return createCodeModuleDtos.stream().map(createCodeModuleDto -> {
      ThunderAnalyzer thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.TYPE_CHECKER, ThunderAnalyzerMode.CLIENT);

      return ThunderUtils.buildCodeModulesDto(
        createCodeModuleDto.uuid(),
        createCodeModuleDto.code(),
        thunderAnalyzer.analyze(
          ThunderUtils.buildCharStream(Collections.singletonList(createCodeModuleDto))
        ).orElse(new CodeModules(Collections.singletonList(new ClientCodeModule()), new ArrayList<>())),
        thunderAnalyzer.getErrors()
      );
    }).toList();
  }

  @Operation(
    summary = "Exports a project",
    description = "Exports a project"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "The project was exported successfully",
      useReturnTypeSchema = true
    )
  })
  @PostMapping("/exportProject")
  ExportedProjectDto exportProject(@RequestBody ExportProjectDto exportProjectDto) {
    RainAnalyzer rainAnalyzer = new RainAnalyzer(CompilerPhase.TYPE_CHECKER);
    Optional<Project> projectOptional = rainAnalyzer.analyze(exportProjectDto);

    if (projectOptional.isEmpty()) {
      return new ExportedProjectDto(
        new ArrayList<>(),
        rainAnalyzer.getErrors().stream().map(CompilerError::toDto).toList()
      );
    }

    String appFileName = switch (exportProjectDto.exportTarget()) {
      case REACT_TYPESCRIPT -> "app.tsx";
    };

    String viewsFileName = switch (exportProjectDto.exportTarget()) {
      case REACT_TYPESCRIPT -> "views.tsx";
    };

    return new ExportedProjectDto(
      List.of(
        new ExportedFileDto(
          projectOptional.get().translate(rainAnalyzer.getSymbolTable(), 0, exportProjectDto.exportTarget()),
          appFileName
        ),
        new ExportedFileDto(
          projectOptional.get().translateViews(appFileName, exportProjectDto.exportTarget()),
          viewsFileName
        )
      ),
      new ArrayList<>()
    );
  }

  @Operation(
    summary = "Exports a project",
    description = "Exports a project"
  )
  @ApiResponses(value = {
    @ApiResponse(
      responseCode = "200",
      description = "The project was exported successfully",
      useReturnTypeSchema = true
    )
  })
  @PostMapping("/translateProject")
  ExportedProjectDto translateProject(@RequestBody TranslateProjectDto translateProjectDto) {
    RainAnalyzer rainAnalyzer = new RainAnalyzer(CompilerPhase.TYPE_CHECKER);
    Optional<Project> projectOptional = rainAnalyzer.analyze(CharStreams.fromString(translateProjectDto.code()));

    if (projectOptional.isEmpty()) {
      return new ExportedProjectDto(
        new ArrayList<>(),
        rainAnalyzer.getErrors().stream().map(CompilerError::toDto).toList()
      );
    }

    return new ExportedProjectDto(
      projectOptional.get().translateServers(
        rainAnalyzer.getSymbolTable(),
        translateProjectDto.exportTarget()
      ).stream().map(code -> new ExportedFileDto(code, "routes.tsx")).toList(),
      new ArrayList<>()
    );
  }
}
