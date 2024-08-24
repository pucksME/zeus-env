package zeus.zeuscompiler.services;

import zeus.zeuscompiler.providers.SymbolTableProvider;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.ServerSymbolTable;
import zeus.zeuscompiler.symboltable.SymbolTable;

import java.util.HashMap;
import java.util.Map;

public class SymbolTableService implements Service {
  private final Map<String, SymbolTable> symbolTables;
  private SymbolTable contextSymbolTable;

  public SymbolTableService() {
    this.symbolTables = new HashMap<>();
  }

  public SymbolTableProvider getContextSymbolTableProvider() {
    return new SymbolTableProvider(this.contextSymbolTable);
  }

  @Override
  public void reset() {
    this.symbolTables.clear();
    this.contextSymbolTable = (this.contextSymbolTable instanceof ClientSymbolTable)
      ? new ClientSymbolTable()
      : new ServerSymbolTable();
  }
}
