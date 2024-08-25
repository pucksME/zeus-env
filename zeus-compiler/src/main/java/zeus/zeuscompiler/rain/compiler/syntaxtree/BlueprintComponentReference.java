package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.UnknownBlueprintComponentException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.mutations.ComponentMutation;
import zeus.zeuscompiler.rain.compiler.syntaxtree.mutations.ShapeMutation;
import zeus.zeuscompiler.rain.dtos.ExportBlueprintComponentReferenceDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
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

  public void check() {
    if (!ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ClientSymbolTable.class).containsBlueprintComponent(blueprintComponentName)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        0,
        0,
        new UnknownBlueprintComponentException(this.blueprintComponentName),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    List<String> translatedMutations = Stream
      .concat(this.componentMutations.stream(), this.shapeMutations.stream())
      .map(mutation -> mutation.translate(depth + 2, exportTarget))
      .filter(translatedMutation -> !translatedMutation.isEmpty())
      .toList();

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "<%s%s/>",
        this.blueprintComponentName,
        (!translatedMutations.isEmpty())
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
