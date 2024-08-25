package zeus.zeuscompiler.rain.compiler.syntaxtree.positions;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

public class SortedPosition extends Position {
  int sorting;
  public SortedPosition(float x, float y, int sorting) {
    super(x, y);
    this.sorting = -sorting;
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s,%s: %s",
        super.translate(depth, exportTarget),
        "\n" + CompilerUtils.buildLinePadding(depth) + "zIndex",
        this.sorting
      );
    };
  }
}
