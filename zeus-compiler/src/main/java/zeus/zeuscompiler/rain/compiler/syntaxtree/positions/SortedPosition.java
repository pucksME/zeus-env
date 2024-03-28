package zeus.zeuscompiler.rain.compiler.syntaxtree.positions;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

public class SortedPosition extends Position {
  int sorting;
  public SortedPosition(float x, float y, int sorting) {
    super(x, y);
    this.sorting = -sorting;
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s,%s: %s",
        super.translate(symbolTable, depth, exportTarget),
        "\n" + CompilerUtils.buildLinePadding(depth) + "zIndex",
        this.sorting
      );
    };
  }
}
