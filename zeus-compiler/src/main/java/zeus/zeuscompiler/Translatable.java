package zeus.zeuscompiler;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

public interface Translatable {
  String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget);
}
