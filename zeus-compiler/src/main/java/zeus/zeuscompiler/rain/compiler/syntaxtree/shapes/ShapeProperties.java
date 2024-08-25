package zeus.zeuscompiler.rain.compiler.syntaxtree.shapes;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.rain.dtos.ExportShapePropertyDto;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShapeProperties implements Translatable {
  Map<ShapeProperty, String> properties;

  public ShapeProperties(Map<ShapeProperty, String> properties) {
    this.properties = properties;
  }

  boolean getBooleanPropertyValue(ShapeProperty shapeProperty) {
    String property = this.properties.get(shapeProperty);
    return property != null && property.equals("true");
  }

  String translateShadowShapeProperty() {
    if (!this.getBooleanPropertyValue(ShapeProperty.SHADOW_ENABLED)) {
      return "none";
    }

    String x = this.properties.get(ShapeProperty.SHADOW_X);
    String y = this.properties.get(ShapeProperty.SHADOW_Y);
    String blur = this.properties.get(ShapeProperty.SHADOW_BLUR);
    String color = this.properties.get(ShapeProperty.SHADOW_COLOR);

    if (x == null || y == null || blur == null || color == null) {
      return "none";
    }

    return String.format("%spx %spx %spx %s", x, y, blur, color);
  }

  String translateBorderRadiusShapeProperty() {
    String borderRadius = this.properties.get(ShapeProperty.BORDER_RADIUS);

    if (borderRadius == null) {
      return "";
    }

    String[] borderRadiusCorners = borderRadius.split(",");

    if (borderRadiusCorners.length != 4) {
      return "";
    }

    return String.format(
      "%s",
      Arrays.stream(borderRadiusCorners).map(
        borderRadiusCorner -> borderRadiusCorner + "px"
      ).collect(Collectors.joining(" "))
    );
  }

  String translateShapeProperty(ShapeProperty shapeProperty, String value, ExportTarget exportTarget) {
    switch (exportTarget) {
      case REACT_TYPESCRIPT -> {
        String propertyFormat = "%s: '%s'";
        String propertyFormatNumeric = "%s: %s";
        return switch (shapeProperty) {
          case BACKGROUND_COLOR_ENABLED, BORDER_ENABLED, SHADOW_ENABLED, SHADOW_X, SHADOW_Y, SHADOW_BLUR, TEXT -> "";
          case HEIGHT -> String.format(propertyFormatNumeric, "height", value);
          case WIDTH -> String.format(propertyFormatNumeric, "width", value);
          case BACKGROUND_COLOR -> String.format(
            propertyFormat,
            "backgroundColor",
            (this.getBooleanPropertyValue(ShapeProperty.BACKGROUND_COLOR_ENABLED) ? value : "transparent")
          );
          case BORDER_COLOR -> String.format(
            propertyFormat,
            "borderColor",
            (this.getBooleanPropertyValue(ShapeProperty.BORDER_ENABLED)) ? value : "transparent"
          );
          case BORDER_WIDTH -> String.format(propertyFormatNumeric, "borderWidth", value);
          case SHADOW_COLOR -> String.format(propertyFormat, "boxShadow", this.translateShadowShapeProperty());
          case OPACITY -> String.format(propertyFormatNumeric, "opacity", value);
          case VISIBLE -> String.format(
            propertyFormat,
            "visibility",
            (this.getBooleanPropertyValue(shapeProperty) ? "inherit" : "hidden")
          );
          case BORDER_RADIUS -> String.format(propertyFormat, "borderRadius", this.translateBorderRadiusShapeProperty());
          case FONT_FAMILY -> String.format(propertyFormat, "fontFamily", value);
          case FONT_SIZE -> String.format(propertyFormatNumeric, "fontSize", value);
          case FONT_STYLE -> String.format(propertyFormat, "fontStyle", value);
          case TEXT_DECORATION -> String.format(propertyFormat, "textDecoration", value);
          case TEXT_TRANSFORM -> String.format(propertyFormat, "textTransform", value);
          case TEXT_ALIGN -> String.format(propertyFormat, "textAlign", value);
          case TEXT_COLOR -> String.format(propertyFormat, "color", value);
        };
      }
    };
    return "";
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> (!this.properties.isEmpty())
        ? CompilerUtils.buildLinePadding(depth) + this.properties.entrySet().stream()
          .map(property -> this.translateShapeProperty(property.getKey(), property.getValue(), exportTarget))
          .filter(translatedProperty -> !translatedProperty.isEmpty())
          .collect(Collectors.joining(",\n" + CompilerUtils.buildLinePadding(depth)))
        : "";
    };
  }

  public static ShapeProperties fromDtos(List<ExportShapePropertyDto> exportShapePropertyDtos) {
    return new ShapeProperties(exportShapePropertyDtos.stream().collect(
      Collectors.toMap(ExportShapePropertyDto::key, ExportShapePropertyDto::value, (first, second) -> second)
    ));
  }

  public void setProperty(ShapeProperty shapeProperty, String value) {
    this.properties.put(shapeProperty, value);
  }

  public Map<ShapeProperty, String> getProperties() {
    return properties;
  }
}
