package zeus.zeuscompiler;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;

public interface Translatable {
  String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget);
}
