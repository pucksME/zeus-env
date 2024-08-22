package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.dtos.CodeModuleDto;

import java.util.List;

public abstract class CodeModule extends TypeCheckableNode implements Convertable<CodeModuleDto>, Translatable {
  String id;
  String description;
  Body body;

  public CodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition);
    this.id = id;
    this.description = description;
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (BodyComponent bodyComponent : this.body.bodyComponents) {
      bodyComponent.checkTypes(symbolTable, compilerErrors);
    }
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description.replaceFirst("::", "");
  }
}
