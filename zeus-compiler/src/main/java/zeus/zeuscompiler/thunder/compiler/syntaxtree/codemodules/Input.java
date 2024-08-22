package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.dtos.PortDto;

import java.util.List;

public class Input extends HeadComponent implements Convertable<PortDto> {
  public Input(int line, int linePosition, String id, Type type) {
    super(line, linePosition, id, type);
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.type.checkType(symbolTable, compilerErrors);
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s: %s",
        this.getId(),
        this.getType().translate(symbolTable, depth, exportTarget)
      );
    };
  }

  @Override
  public PortDto toDto() {
    return new PortDto(this.id, this.type.toDto());
  }
}
