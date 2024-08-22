package zeus.zeuscompiler;

import zeus.zeuscompiler.symboltable.ServerSymbolTable;
import zeus.zeuscompiler.symboltable.SymbolTable;

import java.util.Map;
import java.util.Optional;

public class SymbolTableService {
  private Map<String, SymbolTable> symbolTables;
  private SymbolTable currentSymbolTable;

  public Optional<SymbolTable> getCurrentServerSymbolTable() {
    if (!(this.currentSymbolTable instanceof ServerSymbolTable)) {
      return Optional.empty();
    }

    return Optional.of(currentSymbolTable);
  }
}
