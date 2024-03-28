package zeus.zeuscompiler.rain.compiler.utils;

public abstract class RainUtils {
  public static String extractPxNumber(String numberPx) {
    return numberPx.replaceFirst("px", "");
  }

  public static String extractPercentNumber(String numberPercent) {
    return numberPercent.replaceFirst("%", "");
  }

  public static String extractTextValue(String valueText) {
    return valueText.replaceAll("\"", "");
  }

  public static String extractCodeModuleCode(String code) {
    return code.replaceAll("`", "");
  }
}
