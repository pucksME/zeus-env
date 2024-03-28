package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementType;
import zeus.zeuscompiler.rain.dtos.ExportProjectDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Project extends Node {
  List<Element> elements;
  List<View> views;

  public Project(
    int line,
    int linePosition,
    String name,
    List<Element> elements,
    List<View> views
  ) {
    super(line, linePosition, name);
    this.elements = elements;
    this.views = views;
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
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

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
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
      exportProjectDto.exportViewDtos().stream().map(View::fromDto).toList()
    );
  }
}
