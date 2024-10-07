package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public abstract class RoutingCodeModule extends ClientCodeModule {
  public RoutingCodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition, id, description);
  }

  protected Optional<Type> evaluatePortType(HeadComponent headComponent, List<String> identifiers) {
    ObjectType objectType;

    if (headComponent.type instanceof IdType) {
      Optional<TypeInformation> typeInformationOptional = ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(ServerRouteSymbolTable.class)
        .getType(this, ((IdType) headComponent.type).getId());

      if (typeInformationOptional.isEmpty()) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new UnknownTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      objectType = typeInformationOptional.get().getType();
    } else {
      objectType = (ObjectType) headComponent.type;
    }

    Type type = objectType;

    for (String identifier : identifiers) {
      if (!(type instanceof ObjectType)) {
        break;
      }

      Optional<Type> typeOptional = ((ObjectType) type).getPropertyType(identifier);

      if (typeOptional.isEmpty()) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new UnknownTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return Optional.empty();
      }

      type = typeOptional.get();
    }

    return Optional.of(type);
  }
}
