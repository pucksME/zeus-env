package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementType;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.SortedPosition;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.Shape;
import zeus.zeuscompiler.rain.dtos.ExportBlueprintComponentDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BlueprintComponent extends Element {
  List<Element> elements;

  public BlueprintComponent(int line, int linePosition, String name, Position position, List<Element> elements) {
    super(line, linePosition, name, position);
    this.elements = elements;
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (symbolTable.addCurrentComponentName(this)) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new AmbiguousElementException(this.getName(), AmbiguousElementType.BLUEPRINT_COMPONENT),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    for (Element element : this.elements) {
      element.check(symbolTable, compilerErrors);
    }
  }

  @Override
  public String translateReference(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "<%s mutations={{",
            CompilerUtils.buildLinePadding(depth + 2) + "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "}}/>"
          },
          0
        ),
        this.name,
        this.getDescendantNames().stream().map(
          name -> String.format("%s: (properties.mutations) ? properties.mutations.%s : undefined", name, name)
        ).collect(Collectors.joining(",\n" + CompilerUtils.buildLinePadding(depth + 2)))
      );
    };
  }

  Set<String> getDescendantNames() {
    Set<String> descendantNames = new HashSet<>();
    for (Element element : this.elements) {
      descendantNames.add(element.getName());
      if (element instanceof BlueprintComponent) {
        descendantNames.addAll(((BlueprintComponent) element).getDescendantNames());
      }
    }
    return descendantNames;
  }

  String translateProperties(ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "properties: {%s}",
        String.format(
          "mutations?: {%s}",
          Stream.concat(Set.of(this.name).stream(), this.getDescendantNames().stream()).map(
            descendantName -> String.format("%s?: mutation", descendantName)
          ).collect(Collectors.joining(", "))
        )
      );
    };
  }

  String translateStyle(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(new String[]{"%s,", "%s"}, 2),
        this.position.translate(symbolTable, depth + 2, exportTarget),
        CompilerUtils.buildLinePadding(depth) + String.format(
          "...((properties.mutations && properties.mutations.%s) ? properties.mutations.%s.style : undefined)",
          this.getName(),
          this.getName()
        )
      );
    };
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    String translatedComponents = this.elements.stream()
      .filter(element -> !(element instanceof Shape))
      .map(element -> element.translate(symbolTable, depth + 1, exportTarget))
      .collect(Collectors.joining("\n"));

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "function %s(%s) {",
            "%s",
            CompilerUtils.buildLinePadding(1) + "return <div style={{",
            "%s",
            CompilerUtils.buildLinePadding(1) + "}}>",
            CompilerUtils.buildLinePadding(2) + "%s",
            CompilerUtils.buildLinePadding(1) + "</div>;",
            "}"
          },
          depth
        ),
        this.name,
        this.translateProperties(exportTarget),
        (!translatedComponents.isEmpty()) ? translatedComponents + "\n" : "",
        this.translateStyle(symbolTable, depth, exportTarget),
        this.elements.stream().map(
          element -> (element instanceof Shape)
            ? element.translate(symbolTable, depth + 1, exportTarget)
            : element.translateReference(symbolTable, depth + 1, exportTarget)
        ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)))
      );
    };
  }

  public static BlueprintComponent fromDto(ExportBlueprintComponentDto exportBlueprintComponentDto) {
    return new BlueprintComponent(
      -1,
      -1,
      exportBlueprintComponentDto.getName(),
      new SortedPosition(exportBlueprintComponentDto.getPositionX(), exportBlueprintComponentDto.getPositionY(), exportBlueprintComponentDto.getSorting()),
      exportBlueprintComponentDto.getExportElementDtos().stream().map(
        exportElementDto -> Element.fromDto(exportElementDto, true)
      ).toList()
    );
  }
}
