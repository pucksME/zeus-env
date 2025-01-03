package zeus.zeuscompiler.symboltable;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RoutingCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ServerRouteSymbolTable extends SymbolTable {
  String serverName;
  String routeId;
  List<String> bootsSpecificationClasses;
  UmbrellaSpecifications umbrellaSpecifications;
  Map<String, Type> currentQuantifierVariableTypes;

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

  public String getServerName() {
    return serverName;
  }

  public void setServerName(String serverName) {
    this.serverName = serverName;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String routeId) {
    this.routeId = routeId;
  }

  public List<String> getBootsSpecificationClasses() {
    return bootsSpecificationClasses;
  }

  public UmbrellaSpecifications getUmbrellaSpecifications() {
    return umbrellaSpecifications;
  }

  public void setUmbrellaSpecifications(UmbrellaSpecifications umbrellaSpecifications) {
    this.umbrellaSpecifications = umbrellaSpecifications;
  }

  public void setCurrentQuantifierVariableTypes(Map<String, Type> currentQuantifierVariableTypes) {
    this.currentQuantifierVariableTypes = currentQuantifierVariableTypes;
  }

  public void resetCurrentQuantifierVariableTypes() {
    this.currentQuantifierVariableTypes = null;
  }

  public Optional<Map<String, Type>> getCurrentQuantifierVariableTypes() {
    return Optional.ofNullable(this.currentQuantifierVariableTypes);
  }
}
