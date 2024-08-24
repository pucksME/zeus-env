package zeus.zeuscompiler.providers;

import zeus.zeuscompiler.symboltable.SymbolTable;

public class SymbolTableProvider {
  SymbolTable symbolTable;

  public SymbolTableProvider(SymbolTable symbolTable) {
    this.symbolTable = symbolTable;
  }

  public <T extends SymbolTable> T provide(Class<T> symbolTableClass) {
    if (symbolTableClass != this.symbolTable.getClass()) {
      throw new SymbolTableUnavailableException();
    }

    return (T) this.symbolTable;
  }
}
