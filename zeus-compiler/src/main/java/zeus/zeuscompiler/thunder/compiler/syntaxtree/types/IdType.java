package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.SymbolTable;
import zeus.zeuscompiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownTypeException;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class IdType extends Type {
  String id;

  public IdType(int line, int linePosition, String id) {
    super(line, linePosition);
    this.id = id;
  }

  @Override
  public void checkType() {
    if (!ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).containsType(ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(SymbolTable.class).getCurrentCodeModule(), this.id)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public boolean compatible(Type type) {
    Optional<TypeInformation> thisTypeInformationOptional = ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).getType(
        ServiceProvider
          .provide(SymbolTableService.class).getContextSymbolTableProvider()
          .provide(SymbolTable.class).getCurrentCodeModule(),
        this.id
    );

    if (thisTypeInformationOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return false;
    }

    Type thisType = thisTypeInformationOptional.get().getType();

    if (type instanceof IdType) {
      Optional<TypeInformation> typeInformationOptional = ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(SymbolTable.class).getType(
          ServiceProvider
            .provide(SymbolTableService.class).getContextSymbolTableProvider()
            .provide(SymbolTable.class).getCurrentCodeModule(),
          ((IdType) type).id
      );

      if (typeInformationOptional.isEmpty()) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          type.getLine(),
          type.getLinePosition(),
          new UnknownTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return false;
      }

      type = typeInformationOptional.get().getType();
    }

    return thisType.compatible(type);
  }

  @Override
  public Optional<Type> getType(List<String> identifiers) {
    throw new RuntimeException("Could not get type from id type: unsupported");
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.id;
    };
  }

  @Override
  public String toString() {
    return this.id;
  }

  public String getId() {
    return id;
  }
}
