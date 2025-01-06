package zeus.zeuscompiler.rain.compiler.syntaxtree.positions;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.List;

public class Position implements Translatable {
  Float x;
  Float y;

  public Position() {
    this.x = null;
    this.y = null;
  }

  public Position(Float x, Float y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    List<String> coordinates = new ArrayList<>();

    if (this.x != null) {
      coordinates.add(switch (exportTarget) {
        case REACT_TYPESCRIPT -> String.format("\n%sleft: %s", CompilerUtils.buildLinePadding(depth), this.x);
      });
    }

    if (this.y != null) {
      coordinates.add(switch (exportTarget) {
        case REACT_TYPESCRIPT -> String.format("\n%stop: %s", CompilerUtils.buildLinePadding(depth), this.y);
      });
    }

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> (!coordinates.isEmpty())
        ? String.format("position: 'absolute',%s", String.join(",", coordinates))
        : "";
    };
  }
}
