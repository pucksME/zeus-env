package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.UnknownBlueprintComponentException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.mutations.ComponentMutation;
import zeus.zeuscompiler.rain.compiler.syntaxtree.mutations.ShapeMutation;
import zeus.zeuscompiler.rain.dtos.ExportBlueprintComponentReferenceDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.stream.Stream;

public class BlueprintComponentReference implements Translatable {
  String blueprintComponentName;
  List<ComponentMutation> componentMutations;
  List<ShapeMutation> shapeMutations;

  public BlueprintComponentReference(
    String blueprintComponentName,
    List<ComponentMutation> componentMutations,
    List<ShapeMutation> shapeMutations
  ) {
    this.blueprintComponentName = blueprintComponentName;
    this.componentMutations = componentMutations;
    this.shapeMutations = shapeMutations;
  }

  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (!symbolTable.containsBlueprintComponent(blueprintComponentName)) {
      compilerErrors.add(new CompilerError(
        0,
        0,
        new UnknownBlueprintComponentException(this.blueprintComponentName),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    List<String> translatedMutations = Stream
      .concat(this.componentMutations.stream(), this.shapeMutations.stream())
      .map(mutation -> mutation.translate(symbolTable, depth + 2, exportTarget))
      .filter(translatedMutation -> !translatedMutation.isEmpty())
      .toList();

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "<%s%s/>",
        this.blueprintComponentName,
        (translatedMutations.size() != 0)
          ? " mutations={{\n" + String.format(
            CompilerUtils.buildLinesFormat(
              new String[]{
                CompilerUtils.buildLinePadding(1) + "%s",
                "}}"
              },
              depth + 1
            ),
              String.join(",\n" + CompilerUtils.buildLinePadding(depth + 2), translatedMutations)
            )
          : ""
      );
    };
  }

  public static BlueprintComponentReference fromDto(
    ExportBlueprintComponentReferenceDto exportBlueprintComponentReferenceDto
  ) {
    return new BlueprintComponentReference(
      exportBlueprintComponentReferenceDto.blueprintComponentName(),
      exportBlueprintComponentReferenceDto.exportComponentMutationDtos().stream().map(
        ComponentMutation::fromDto
      ).toList(),
      exportBlueprintComponentReferenceDto.exportShapeMutationDtos().stream().map(ShapeMutation::fromDto).toList()
    );
  }
}
