package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.rain.dtos.ExportTarget;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class UmbrellaSpecifications extends Node {
  Map<String, UmbrellaSpecification> umbrellaSpecifications;

  public UmbrellaSpecifications(int line, int linePosition, Map<String, UmbrellaSpecification> umbrellaSpecifications) {
    super(line, linePosition);
    this.umbrellaSpecifications = umbrellaSpecifications;
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {

  }

  @Override
  public String translate(zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }

  private void initializeUmbrellaSpecification(String id, int line, int linePosition) {
    if (this.umbrellaSpecifications.containsKey(id)) {
      return;
    }

    this.umbrellaSpecifications.put(id, new UmbrellaSpecification(line, linePosition, id));
  }

  public void setUmbrellaSpecificationFormula(String id, int line, int linePosition, Formula formula) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setFormula(formula);
  }

  public void setUmbrellaSpecificationContext(String id, int line, int linePosition, Context context) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setContext(context);
  }

  public void setUmbrellaSpecificationAction(String id, int line, int linePosition, Action action) {
    initializeUmbrellaSpecification(id, line, linePosition);
    this.umbrellaSpecifications.get(id).setAction(action);
  }
}
