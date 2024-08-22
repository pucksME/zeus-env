package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.ShapeProperties;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.ShapeProperty;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.rain.dtos.ExportViewDto;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class View extends Node {
  float height;
  float width;
  Position position;
  boolean isRoot;
  List<Element> elements;

  public View(
    int line,
    int linePosition,
    String name,
    float height,
    float width,
    Position position,
    boolean isRoot,
    List<Element> elements
  ) {
    super(line, linePosition, name);
    this.height = height;
    this.width = width;
    this.position = position;
    this.isRoot = isRoot;
    this.elements = elements;
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (Element element : this.elements) {
      symbolTable.setCurrentComponent(element);
      element.check(symbolTable, compilerErrors);
    }
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        List<String> translatedElements = this.elements.stream()
          .filter(element -> element instanceof Component && ((Component) element).blueprintComponentReference == null)
          .map(element -> element.translate(symbolTable, depth + 1, exportTarget))
          .toList();

        yield CompilerUtils.trimLines(String.format(
          CompilerUtils.buildLinesFormat(
            new String[]{
              "export function %s() {",
              "%s",
              CompilerUtils.buildLinePadding(1) + "return <div style={{",
              "%s",
              CompilerUtils.buildLinePadding(1) + "}}>",
              CompilerUtils.buildLinePadding(2) + "%s",
              CompilerUtils.buildLinePadding(1) + "</div>",
              "}"
            },
            depth
          ),
          this.name,
          (!translatedElements.isEmpty()) ? String.join("\n", translatedElements) : "",
          new ShapeProperties(Map.of(
            ShapeProperty.HEIGHT, String.valueOf(this.height),
            ShapeProperty.WIDTH, String.valueOf(this.width)
          )).translate(symbolTable, depth + 2, exportTarget),
          this.elements.stream().map(
            element -> element.translateReference(symbolTable, depth + 1, exportTarget)
          ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)))
        ));
      }
    };
  }

  public static View fromDto(ExportViewDto exportViewDto) {
    return new View(
      -1,
      -1,
      exportViewDto.name(),
      exportViewDto.height(),
      exportViewDto.width(),
      new Position(exportViewDto.positionX(), exportViewDto.positionY()),
      exportViewDto.isRoot(),
      exportViewDto.exportElementDtos().stream().map(
        exportElementDto -> Element.fromDto(exportElementDto, false)
      ).toList()
    );
  }
}
