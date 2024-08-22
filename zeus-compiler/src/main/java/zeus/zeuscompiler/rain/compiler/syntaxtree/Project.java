package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementType;
import zeus.zeuscompiler.rain.dtos.*;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Project extends Node {
  List<Element> elements;
  List<View> views;
  List<Server> servers;

  public Project(
    int line,
    int linePosition,
    String name,
    List<Element> elements,
    List<View> views,
    List<Server> servers
  ) {
    super(line, linePosition, name);
    this.elements = elements;
    this.views = views;
    this.servers = servers;
  }

  private void checkServers(List<CompilerError> compilerErrors) {
    Set<String> serverNames = new HashSet<>();

    for (Server server : this.servers) {
      if (serverNames.add(server.name)) {
        continue;
      }

      compilerErrors.add(new CompilerError(
        server.line,
        server.linePosition,
        new AmbiguousElementException(server.name, AmbiguousElementType.SERVER),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (Element element : this.elements) {
      if (symbolTable.addBlueprintComponent((BlueprintComponent) element)) {
        compilerErrors.add(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new AmbiguousElementException(element.name, AmbiguousElementType.BLUEPRINT_COMPONENT),
          CompilerPhase.TYPE_CHECKER
        ));
      }

      symbolTable.setCurrentComponent(element);
      element.check(symbolTable, compilerErrors);
    }

    for (View view : this.views) {
      if (symbolTable.addView(view)) {
        compilerErrors.add(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new AmbiguousElementException(view.name, AmbiguousElementType.VIEW),
          CompilerPhase.TYPE_CHECKER
        ));
      }

      view.check(symbolTable, compilerErrors);
    }

    this.checkServers(compilerErrors);
  }

  public String translateViews(String appFileName, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(new String[]{"%s", "%s", "%s", "%s", "%s"}, 0),
        // https://reactrouter.com/en/main/start/tutorial#adding-a-router [accessed 09/01/2024, 17:31]
        "import { createBrowserRouter } from 'react-router-dom';",
        String.format(
          "import { %s } from './%s';",
          this.views.stream().map(view -> view.name).collect(Collectors.joining(", ")),
          appFileName
        ),
        "export const router = createBrowserRouter([",
        this.views.stream().map(view -> String.format(
          CompilerUtils.buildLinePadding(1) + "{ path: '/%s', element: <%s/> }",
          (view.isRoot) ? "" : view.name,
          view.name
        )).collect(Collectors.joining(",\n")),
        "]);"
      );
    };
  }

  public List<ExportedClientDto> translateClients(ClientSymbolTable symbolTable, ExportTarget exportTarget) {
    String appFileName = switch (exportTarget) {
      case REACT_TYPESCRIPT -> "app.tsx";
    };

    return List.of(new ExportedClientDto(List.of(
      new ExportedFileDto(this.translate(symbolTable, 0, exportTarget), appFileName),
      new ExportedFileDto(this.translateViews(appFileName, exportTarget), "views.tsx")
    )));
  }

  public List<ExportedServerDto> translateServers(ClientSymbolTable symbolTable, ExportTarget exportTarget) {
    return this.servers.stream()
      .map(server -> new ExportedServerDto(
        server.name,
        List.of(
          new ExportedFileDto(server.translateConfiguration(0, exportTarget), "configuration.ts"),
          new ExportedFileDto(server.translate(symbolTable, 0, exportTarget), "routes.ts")
        ),
        server.translateBootsSpecifications().entrySet().stream()
          .flatMap(bootsSpecificationTranslations -> bootsSpecificationTranslations.getValue().entrySet().stream()
            .map(bootsSpecificationTranslation -> new ExportedFileDto(
              bootsSpecificationTranslation.getValue(),
              String.format("%s-%s.py", bootsSpecificationTranslations.getKey(), bootsSpecificationTranslation.getKey())
            )))
          .toList()
      ))
      .toList();
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(new String[]{"%s", "%s", "%s", "%s"}, depth),
        "import React, {CSSProperties} from 'react';",
        "type mutation = {style: CSSProperties, data?: {text?: string}}",
        CompilerUtils.trimLines(this.elements.stream().map(
          element -> element.translate(symbolTable, depth, exportTarget)
        ).collect(Collectors.joining("\n"))),
        this.views.stream().map(
          view -> view.translate(symbolTable, depth, exportTarget)
        ).collect(Collectors.joining("\n"))
      );
    };
  }

  public static Project fromDto(ExportProjectDto exportProjectDto) {
    return new Project(
      -1,
      -1,
      exportProjectDto.name(),
      exportProjectDto.exportElementDtos().stream().map(
        exportElementDto -> Element.fromDto(exportElementDto, true)
      ).toList(),
      exportProjectDto.exportViewDtos().stream().map(View::fromDto).toList(),
      new ArrayList<>()
    );
  }
}
