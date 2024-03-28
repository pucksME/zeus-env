package zeus.zeuscompiler.utils;

import java.util.Arrays;
import java.util.stream.Collectors;

public abstract class CompilerUtils {
  public static String buildLinePadding(int depth) {
    // https://stackoverflow.com/a/6857936 [accessed 20/9/2023, 08:02]
    // https://stackoverflow.com/a/49065337 [accessed 20/9/2023, 08:02]
    return "  ".repeat(depth);
  }

  public static String buildLinesFormat(String[] lines, int depth) {
    return Arrays.stream(lines).map(
      line -> CompilerUtils.buildLinePadding(depth) + line
    ).collect(Collectors.joining("\n"));
  }

  public static String trimLines(String lines) {
    return Arrays
      .stream(lines.split("\n"))
      .filter(line -> !line.isBlank())
      .collect(Collectors.joining("\n"));
  }
}
