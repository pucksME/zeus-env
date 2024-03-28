package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementType;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.SortedPosition;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.Shape;
import zeus.zeuscompiler.rain.dtos.ExportComponentDto;
import zeus.zeuscompiler.rain.dtos.ExportComponentMutationDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Component extends Element {
  BlueprintComponentReference blueprintComponentReference;
  String code;
  List<Element> elements;

  public Component(
    int line,
    int linePosition,
    String name,
    Position position,
    BlueprintComponentReference blueprintComponentReference,
    String code,
    List<Element> elements
  ) {
    super(line, linePosition, name, position);
    this.blueprintComponentReference = blueprintComponentReference;
    this.code = code;
    this.elements = elements;
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (this.blueprintComponentReference != null) {
      this.blueprintComponentReference.check(symbolTable, compilerErrors);
    }

    if (symbolTable.addCurrentComponentName(this)) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new AmbiguousElementException(this.getName(), AmbiguousElementType.COMPONENT),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    if (this.code != null) {
      ThunderAnalyzer thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.TYPE_CHECKER);
      thunderAnalyzer.analyze(this.code);
      compilerErrors.addAll(thunderAnalyzer.getErrors());
    }

    for (Element element : this.elements) {
      element.check(symbolTable, compilerErrors);
    }
  }

  @Override
  public String translateReference(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> (this.blueprintComponentReference != null)
        ? this.blueprintComponentReference.translate(symbolTable, depth, exportTarget)
        : String.format("<%s/>", this.name);
    };
  }

  public String translateCode(int dept, ExportTarget exportTarget) {
    ThunderAnalyzer thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.TYPE_CHECKER);
    Optional<CodeModules> codeModulesOptional = thunderAnalyzer.analyze(this.code);

    assert !thunderAnalyzer.hasErrors() && codeModulesOptional.isPresent();
    return codeModulesOptional.get().translate(thunderAnalyzer.getSymbolTable(), dept, exportTarget);
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    String translatedComponents = this.elements.stream()
      .filter(element -> !(element instanceof Shape))
      .map(element -> element.translate(symbolTable, depth + 1, exportTarget))
      .collect(Collectors.joining("\n"));

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> (this.blueprintComponentReference == null)
        ? String.format(
          CompilerUtils.buildLinesFormat(
            new String[]{
              "function %s() {\n%s",
              CompilerUtils.buildLinePadding(1) + "%s",
              CompilerUtils.buildLinePadding(1) + "return <div style={{",
              CompilerUtils.buildLinePadding(2) + "%s",
              CompilerUtils.buildLinePadding(1) + "}}>",
              CompilerUtils.buildLinePadding(2) + "%s",
              CompilerUtils.buildLinePadding(1) + "</div>",
              "}"
            },
            depth
          ),
          this.name,
          (!translatedComponents.isEmpty()) ? translatedComponents + "\n" : "",
          (this.code != null) ? this.translateCode(depth, exportTarget) : "",
          this.position.translate(symbolTable, depth + 2, exportTarget),
          this.elements.stream().map(
            element -> (element instanceof Shape)
              ? element.translate(symbolTable, depth + 1, exportTarget)
              : element.translateReference(symbolTable, depth + 1, exportTarget)
          ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)))
        )
        : "";
    };
  }

  public static Component fromDto(ExportComponentDto exportComponentDto) {
    if (exportComponentDto.getExportBlueprintComponentReferenceDto() != null) {
      exportComponentDto.getExportBlueprintComponentReferenceDto().exportComponentMutationDtos().add(
        new ExportComponentMutationDto(
          exportComponentDto.getName(),
          exportComponentDto.getPositionX(),
          exportComponentDto.getPositionY()
        )
      );
    }

    return new Component(
      -1,
      -1,
      exportComponentDto.getName(),
      new SortedPosition(
        exportComponentDto.getPositionX(),
        exportComponentDto.getPositionY(),
        exportComponentDto.getSorting()
      ),
      (exportComponentDto.getExportBlueprintComponentReferenceDto() != null)
        ? BlueprintComponentReference.fromDto(exportComponentDto.getExportBlueprintComponentReferenceDto())
        : null,
      (exportComponentDto.getExportCodeDto() != null) ? exportComponentDto.getExportCodeDto().code() : null,
      exportComponentDto.getExportElementDtos().stream().map(
        exportElementDto -> Element.fromDto(exportElementDto, false)
      ).toList()
    );
  }
}
