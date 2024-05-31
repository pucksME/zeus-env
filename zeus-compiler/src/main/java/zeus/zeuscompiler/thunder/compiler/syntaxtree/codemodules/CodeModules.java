package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.dtos.CodeModuleDto;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CodeModules extends TypeCheckableNode implements Convertable<List<CodeModuleDto>>, Translatable {
  Map<String, ClientCodeModule> clientCodeModules;
  Map<String, InstanceCodeModule> instanceCodeModules;

  public CodeModules() {
    this.clientCodeModules = new HashMap<>();
    this.instanceCodeModules = new HashMap<>();
  }

  public CodeModules(
    List<ClientCodeModule> clientCodeModules,
    List<InstanceCodeModule> instanceCodeModules
  ) {
    // https://www.baeldung.com/java-duplicate-keys-when-producing-map-using-stream#using-thetomapmethod-and-handling-the-duplicated-keys [accessed 28/8/2023, 20:30]
    this.clientCodeModules = clientCodeModules.stream().collect(
      Collectors.toMap(CodeModule::getId, clientCodeModule -> clientCodeModule, (first, second) -> second)
    );

    this.instanceCodeModules = instanceCodeModules.stream().collect(
      Collectors.toMap(CodeModule::getId, instanceCodeModule -> instanceCodeModule, (first, second) -> second)
    );
  }

  public Optional<CodeModule> getCodeModule(String id) {
    CodeModule codeModule = this.clientCodeModules.get(id);

    if (codeModule != null) {
      return Optional.of(codeModule);
    }

    return Optional.ofNullable(this.instanceCodeModules.get(id));
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (CodeModule codeModule : this.getCodeModules()) {
      symbolTable.setCurrentCodeModule(codeModule);
      codeModule.checkTypes(symbolTable, compilerErrors);
    }
  }

  @Override
  public List<CodeModuleDto> toDto() {
    return this.getCodeModules().stream().map(CodeModule::toDto).toList();
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    ArrayList<String> codeModulesCode = new ArrayList<>(symbolTable.getPublicTypes().entrySet().stream().map(
      publicType -> String.format(
        "type %s = %s;",
        publicType.getKey(),
        publicType.getValue().get(0).getType().translate(symbolTable, depth, exportTarget)
      )
    ).toList());

    for (CodeModule codeModule : this.getCodeModules()) {
      symbolTable.setCurrentCodeModule(codeModule);
      codeModulesCode.add(codeModule.translate(symbolTable, depth, exportTarget));
    }

    return String.join("\n" + CompilerUtils.buildLinePadding(depth + 1), codeModulesCode);
  }

  public List<CodeModule> getCodeModules() {
    // https://stackoverflow.com/a/18687790 [accessed 19/4/2023, 12:36]
    return Stream.concat(this.clientCodeModules.values().stream(), this.instanceCodeModules.values().stream()).toList();
  }

  public Map<String, ClientCodeModule> getClientCodeModules() {
    return clientCodeModules;
  }
}
