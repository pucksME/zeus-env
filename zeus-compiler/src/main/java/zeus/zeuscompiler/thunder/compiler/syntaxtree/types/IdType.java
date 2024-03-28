package zeus.zeuscompiler.thunder.compiler.syntaxtree.types;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.symboltable.TypeInformation;
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
  public void checkType(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (!symbolTable.containsType(symbolTable.getCurrentCodeModule(), this.id)) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public boolean compatible(SymbolTable symbolTable, List<CompilerError> compilerErrors, Type type) {
    Optional<TypeInformation> thisTypeInformationOptional = symbolTable.getType(
      symbolTable.getCurrentCodeModule(),
      this.id
    );

    if (thisTypeInformationOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return false;
    }

    Type thisType = thisTypeInformationOptional.get().getType();

    if (type instanceof IdType) {
      Optional<TypeInformation> typeInformationOptional = symbolTable.getType(
        symbolTable.getCurrentCodeModule(),
        ((IdType) type).id
      );

      if (typeInformationOptional.isEmpty()) {
        compilerErrors.add(new CompilerError(
          type.getLine(),
          type.getLinePosition(),
          new UnknownTypeException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return false;
      }

      type = typeInformationOptional.get().getType();
    }

    return thisType.compatible(symbolTable, compilerErrors, type);
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
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
