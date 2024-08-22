package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.symboltable.TypeInformation;
import zeus.zeuscompiler.symboltable.TypeVisibility;

import java.util.List;
import java.util.Optional;

public class DeclarationTypeStatement extends Statement {
  String id;
  boolean isPublic;

  public DeclarationTypeStatement(int line, int linePosition, String id, boolean isPublic) {
    super(line, linePosition);
    this.id = id;
    this.isPublic = isPublic;
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    Optional<TypeInformation> typeInformationOptional = symbolTable.getType(symbolTable.getCurrentCodeModule(), this.id);
    assert typeInformationOptional.isPresent();
    TypeInformation typeInformation = typeInformationOptional.get();

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> (typeInformation.getTypeVisibility() == TypeVisibility.PRIVATE)
        ? String.format("type %s = %s;", this.id, typeInformation.getType().translate(symbolTable, depth, exportTarget))
        : "";
    };
  }

  public boolean isPublic() {
    return isPublic;
  }
}
