package zeus.zeuscompiler.services;

import zeus.zeuscompiler.providers.SymbolTableProvider;
import zeus.zeuscompiler.symboltable.*;

import java.util.HashMap;
import java.util.Map;

public class SymbolTableService implements Service {
  private final Map<SymbolTableIdentifier, SymbolTable> symbolTables;
  private SymbolTable contextSymbolTable;

  public SymbolTableService() {
    this.symbolTables = new HashMap<>();
  }

  public void initializeContextSymbolTable(SymbolTableIdentifier symbolTableIdentifier) {
    this.contextSymbolTable = (symbolTableIdentifier instanceof ServerSymbolTableIdentifier)
      ? new ServerSymbolTable()
      : new ClientSymbolTable();
    this.symbolTables.put(symbolTableIdentifier, this.contextSymbolTable);
  }

  public SymbolTableProvider getContextSymbolTableProvider() {
    return new SymbolTableProvider(this.contextSymbolTable);
  }

  public void restoreContextSymbolTable(SymbolTableIdentifier symbolTableIdentifier) {
    SymbolTable symbolTable = this.symbolTables.get(symbolTableIdentifier);

    if (symbolTable == null) {
      throw new SymbolTableNotFoundException();
    }

    this.contextSymbolTable = symbolTable;
  }

  @Override
  public void reset() {
    this.symbolTables.clear();
    this.contextSymbolTable = (this.contextSymbolTable instanceof ClientSymbolTable)
      ? new ClientSymbolTable()
      : new ServerSymbolTable();
  }
}
