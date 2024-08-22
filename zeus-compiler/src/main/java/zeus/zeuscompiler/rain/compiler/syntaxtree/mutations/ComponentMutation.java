package zeus.zeuscompiler.rain.compiler.syntaxtree.mutations;

import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.dtos.ExportComponentMutationDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

public class ComponentMutation extends Mutation {
  String blueprintComponentName;

  public ComponentMutation(Position position, String blueprintComponentName) {
    super(position);
    this.blueprintComponentName = blueprintComponentName;
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    String translatedPosition = this.position.translate(symbolTable, depth + 2, exportTarget);
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> (!translatedPosition.isEmpty())
        ? String.format(
          CompilerUtils.buildLinesFormat(
            new String[]{
              "%s: {",
              CompilerUtils.buildLinePadding(depth + 1) + "style: {",
              CompilerUtils.buildLinePadding(depth + 2) + "%s",
              CompilerUtils.buildLinePadding(depth + 1) + "}",
              CompilerUtils.buildLinePadding(depth) + "}"
            },
            0
          ),
          this.blueprintComponentName,
        translatedPosition
        )
        : "";
    };
  }

  public static ComponentMutation fromDto(ExportComponentMutationDto exportComponentMutationDto) {
    return new ComponentMutation(
      new Position(exportComponentMutationDto.positionX(), exportComponentMutationDto.positionY()),
      exportComponentMutationDto.blueprintComponentName()
    );
  }
}
