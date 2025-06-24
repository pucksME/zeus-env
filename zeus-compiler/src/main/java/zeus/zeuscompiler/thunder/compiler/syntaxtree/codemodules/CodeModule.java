package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;
import zeus.zeuscompiler.thunder.dtos.CodeModuleDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class CodeModule extends TypeCheckableNode implements Convertable<CodeModuleDto>, Translatable {
  String id;
  String description;
  Body body;
  private Map<String, VariableInformation> variables;

  public CodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition);
    this.id = id;
    this.description = description;
  }

  @Override
  public void checkTypes() {
    for (BodyComponent bodyComponent : this.body.bodyComponents) {
      bodyComponent.checkTypes();
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

  public void setVariables(Map<String, VariableInformation> variables) {
    this.variables = variables;
  }

  public Optional<Map<String, VariableInformation>> getVariables() {
    return Optional.ofNullable(variables);
  }
}
