package zeus.zeuscompiler.symboltable;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RoutingCodeModule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerRouteSymbolTable extends SymbolTable {
  List<String> bootsSpecificationClasses;

  public ServerRouteSymbolTable() {
    this.bootsSpecificationClasses = new ArrayList<>();
  }

  public void addBootsSpecificationClass(String id) {
    this.bootsSpecificationClasses.add(id);
  }

  public <T extends RoutingCodeModule> Optional<T> getRoutingCodeModule(Class<T> routingCodeModuleClass) {
    if (routingCodeModuleClass == RoutingCodeModule.class) {
      throw new InvalidRoutingCodeModuleException();
    }

    Optional<CodeModule> codeModuleOptional = this.codeModules.getCodeModule(
      (routingCodeModuleClass == RequestCodeModule.class) ? "request" : "response"
    );

    if (codeModuleOptional.isEmpty() || codeModuleOptional.get().getClass() != routingCodeModuleClass) {
      return Optional.empty();
    }

    return Optional.of((T) codeModuleOptional.get());
  }
  public List<String> getBootsSpecificationClasses() {
    return bootsSpecificationClasses;
  }
}
