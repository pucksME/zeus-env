package zeus.zeuscompiler.symboltable;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

import java.util.*;

public abstract class SymbolTable {
  Map<String, List<TypeInformation>> publicTypes;
  Map<CodeModule, Map<String, List<TypeInformation>>> types;
  Map<CodeModule, Map<String, List<VariableInformation>>> variables;
  CodeModules codeModules;
  CodeModule currentCodeModule;

  public SymbolTable() {
    this.publicTypes = new HashMap<>();
    this.types = new HashMap<>();
    this.variables = new HashMap<>();
  }

  private void addType(String id, TypeInformation typeInformation) {
    Map<String, List<TypeInformation>> types = (typeInformation.typeVisibility == TypeVisibility.PUBLIC)
      ? this.publicTypes
      : this.types.get(this.currentCodeModule);

    if (!types.containsKey(id)) {
      types.put(id, new ArrayList<>(Collections.singletonList(typeInformation)));
      return;
    }

    types.get(id).add(typeInformation);
  }

  public void addPrivateType(String id, ObjectType objectType) {
    addType(id, new TypeInformation(objectType, TypeVisibility.PRIVATE));
  }

  public void addPublicType(String id, ObjectType objectType) {
    addType(id, new TypeInformation(objectType, TypeVisibility.PUBLIC));
  }

  public void addVariable(String id, Type type, VariableType variableType, int line, int linePosition) {
    Map<String, List<VariableInformation>> variables = this.variables.get(this.currentCodeModule);
    VariableInformation variableInformation = new VariableInformation(type, variableType, line, linePosition);

    if (!variables.containsKey(id)) {
      variables.put(id, new ArrayList<>(Collections.singletonList(variableInformation)));
      return;
    }

    variables.get(id).add(variableInformation);
  }

  public Optional<VariableInformation> getVariable(CodeModule codeModule, String id) {
    List<VariableInformation> variableInformationList = this.variables.get(codeModule).get(id);
    return (variableInformationList != null) ? Optional.of(variableInformationList.get(0)) : Optional.empty();
  }

  public Optional<VariableInformation> getVariable(CodeModule codeModule, String id, int line, int linePosition) {
    Optional<VariableInformation> variableInformationOptional = this.getVariable(codeModule, id);

    if (variableInformationOptional.isEmpty()) {
      return Optional.empty();
    }

    VariableInformation variableInformation = variableInformationOptional.get();

    if (line == variableInformation.declarationLine) {
      if (linePosition > variableInformation.declarationLinePosition) {
        return Optional.of(variableInformation);
      }
      return Optional.empty();
    }

    if (line > variableInformation.declarationLine) {
      return Optional.of(variableInformation);
    }

    return Optional.empty();
  }

  public Optional<TypeInformation> getType(CodeModule codeModule, String id) {
    Map<String, List<TypeInformation>> types = this.types.get(codeModule);

    if (types == null || types.get(id) == null) {
      types = this.publicTypes;
    }

    List<TypeInformation> typeInformationList = types.get(id);

    return (typeInformationList == null) ? Optional.empty() : Optional.of(typeInformationList.get(0));
  }

  public boolean containsType(CodeModule codeModule, String id) {
    return this.getType(codeModule, id).isPresent();
  }

  public void setCurrentCodeModule(CodeModule currentCodeModule) {
    this.currentCodeModule = currentCodeModule;

    if (!this.types.containsKey(this.currentCodeModule)) {
      this.types.put(this.currentCodeModule, new HashMap<>());
    }

    if (!this.variables.containsKey(this.currentCodeModule)) {
      this.variables.put(this.currentCodeModule, new HashMap<>());
    }
  }

  public void setCodeModules(CodeModules codeModules) {
    this.codeModules = codeModules;
  }

  public CodeModules getCodeModules() {
    return this.codeModules;
  }

  public CodeModule getCurrentCodeModule() {
    return currentCodeModule;
  }

  public Map<String, List<TypeInformation>> getPublicTypes() {
    return publicTypes;
  }
}
