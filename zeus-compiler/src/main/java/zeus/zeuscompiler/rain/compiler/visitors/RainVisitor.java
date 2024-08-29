package zeus.zeuscompiler.rain.compiler.visitors;

import zeus.zeuscompiler.bootsspecification.compiler.BootsSpecificationAnalyzer;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.grammars.RainParser;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.compiler.syntaxtree.*;
import zeus.zeuscompiler.rain.compiler.syntaxtree.shapes.*;
import zeus.zeuscompiler.rain.compiler.utils.RainUtils;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTableIdentifier;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTableIdentifier;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzerMode;
import zeus.zeuscompiler.grammars.RainBaseVisitor;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.SortedPosition;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.*;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.UmbrellaSpecificationAnalyzer;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;

import java.util.*;
import java.util.stream.Collectors;

public class RainVisitor extends RainBaseVisitor<Object> {
  ShapeProperties currentShapeProperties;
  String currentServerName;

  public RainVisitor() {
    this.currentShapeProperties = null;
  }

  private void resetCurrentShapeProperties() {
    this.currentShapeProperties = null;
  }

  @Override
  public Object visitProject(RainParser.ProjectContext ctx) {
    ServiceProvider.provide(SymbolTableService.class).initializeContextSymbolTable(new ClientSymbolTableIdentifier());
    return new Project(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      (ctx.blueprintComponents() == null)
        ? new ArrayList<>()
        : ctx.blueprintComponents().blueprintComponent().stream()
          .map(blueprintComponentContext -> (Element) visit(blueprintComponentContext))
          .toList(),
      ctx.view().stream().map(viewContext -> (View) visit(viewContext)).toList(),
      ctx.server().stream().map(serverContext -> (Server) visit(serverContext)).toList()
    );
  }

  @Override
  public Object visitBlueprintComponents(RainParser.BlueprintComponentsContext ctx) {
    return super.visitBlueprintComponents(ctx);
  }

  @Override
  public Object visitBlueprintComponent(RainParser.BlueprintComponentContext ctx) {
    return new BlueprintComponent(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      (ctx.position() == null) ? null : (Position) visit(ctx.position()),
      ctx.blueprintElement().stream().map(blueprintElementContext -> (Element) visit(blueprintElementContext)).toList()
    );
  }

  @Override
  public Object visitPosition(RainParser.PositionContext ctx) {
    float positionXNumber = Float.parseFloat(RainUtils.extractPxNumber(ctx.positionX().NUMBER_PX().getText()));
    float positionYNumber = Float.parseFloat(RainUtils.extractPxNumber(ctx.positionY().NUMBER_PX().getText()));

    return new SortedPosition(
      ctx.positionX().NEGATIVE() == null ? positionXNumber : -positionXNumber,
      ctx.positionY().NEGATIVE() == null ? positionYNumber : -positionYNumber,
      Integer.parseInt(ctx.positionSorting().NUMBER().getText())
    );
  }

  @Override
  public Object visitShapeRectangle(RainParser.ShapeRectangleContext ctx) {
    this.currentShapeProperties = new ShapeProperties(new HashMap<>());
    visit(ctx.shapeRectangleProperties());

    Rectangle rectangle = new Rectangle(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      (Position) visit(ctx.position()),
      this.currentShapeProperties,
      false
    );

    resetCurrentShapeProperties();
    return rectangle;
  }

  @Override
  public Object visitShapeCircle(RainParser.ShapeCircleContext ctx) {
    this.currentShapeProperties = new ShapeProperties(new HashMap<>());
    visit(ctx.shapeCircleProperties());

    Circle circle = new Circle(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      (Position) visit(ctx.position()),
      this.currentShapeProperties,
      false
    );

    resetCurrentShapeProperties();
    return circle;
  }

  @Override
  public Object visitShapeText(RainParser.ShapeTextContext ctx) {
    this.currentShapeProperties = new ShapeProperties(new HashMap<>());
    visit(ctx.shapeTextProperties());

    Text text = new Text(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      (Position) visit(ctx.position()),
      this.currentShapeProperties,
      false
    );

    resetCurrentShapeProperties();
    return text;
  }

  @Override
  public Object visitPropertyBackgroundColor(RainParser.PropertyBackgroundColorContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.BACKGROUND_COLOR, ctx.COLOR().getText());
    return null;
  }

  @Override
  public Object visitPropertyBorderColor(RainParser.PropertyBorderColorContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.BORDER_COLOR, ctx.COLOR().getText());
    return null;
  }

  @Override
  public Object visitPropertyShadowColor(RainParser.PropertyShadowColorContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.SHADOW_COLOR, ctx.COLOR().getText());
    return null;
  }

  @Override
  public Object visitPropertyBorderRadius(RainParser.PropertyBorderRadiusContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.BORDER_RADIUS,
      ctx.propertyBorderRadiusValue().NUMBER_PX().stream()
        .map(numberPx -> RainUtils.extractPxNumber(numberPx.getText()))
        .collect(Collectors.joining(","))
    );
    return null;
  }

  @Override
  public Object visitPropertyHeight(RainParser.PropertyHeightContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.HEIGHT, RainUtils.extractPxNumber(ctx.NUMBER_PX().getText()));
    return null;
  }

  @Override
  public Object visitPropertyWidth(RainParser.PropertyWidthContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.WIDTH, RainUtils.extractPxNumber(ctx.NUMBER_PX().getText()));
    return null;
  }

  @Override
  public Object visitPropertyBorderWidth(RainParser.PropertyBorderWidthContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.BORDER_WIDTH,
      RainUtils.extractPxNumber(ctx.NUMBER_PX().getText())
    );
    return null;
  }

  @Override
  public Object visitPropertyShadowX(RainParser.PropertyShadowXContext ctx) {
    int shadowXNumber = Integer.parseInt(RainUtils.extractPxNumber(ctx.NUMBER_PX().getText()));
    this.currentShapeProperties.setProperty(
      ShapeProperty.SHADOW_X,
      String.valueOf((ctx.NEGATIVE() == null) ? shadowXNumber : -shadowXNumber)
    );
    return null;
  }

  @Override
  public Object visitPropertyShadowY(RainParser.PropertyShadowYContext ctx) {
    int shadowXNumber = Integer.parseInt(RainUtils.extractPxNumber(ctx.NUMBER_PX().getText()));
    this.currentShapeProperties.setProperty(
      ShapeProperty.SHADOW_Y,
      String.valueOf((ctx.NEGATIVE() == null) ? shadowXNumber : -shadowXNumber)
    );
    return null;
  }

  @Override
  public Object visitPropertyShadowBlur(RainParser.PropertyShadowBlurContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.SHADOW_BLUR,
      RainUtils.extractPxNumber(ctx.NUMBER_PX().getText())
    );
    return null;
  }

  @Override
  public Object visitPropertyBackgroundColorEnabled(RainParser.PropertyBackgroundColorEnabledContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.BACKGROUND_COLOR_ENABLED, ctx.boolean_().getText());
    return null;
  }

  @Override
  public Object visitPropertyBorderEnabled(RainParser.PropertyBorderEnabledContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.BORDER_ENABLED, ctx.boolean_().getText());
    return null;
  }

  @Override
  public Object visitPropertyShadowEnabled(RainParser.PropertyShadowEnabledContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.SHADOW_ENABLED, ctx.boolean_().getText());
    return null;
  }

  @Override
  public Object visitPropertyVisible(RainParser.PropertyVisibleContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.VISIBLE, ctx.boolean_().getText());
    return null;
  }

  @Override
  public Object visitPropertyOpacity(RainParser.PropertyOpacityContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.OPACITY,
      RainUtils.extractPercentNumber(ctx.NUMBER_PERCENT().getText())
    );
    return null;
  }

  @Override
  public Object visitPropertyFontFamily(RainParser.PropertyFontFamilyContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.FONT_FAMILY, ctx.PROPERTY_FONT_FAMILY_VALUE().getText());
    return null;
  }

  @Override
  public Object visitPropertyFontStyle(RainParser.PropertyFontStyleContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.FONT_STYLE, ctx.PROPERTY_FONT_STYLE_VALUE().getText());
    return null;
  }

  @Override
  public Object visitPropertyTextDecoration(RainParser.PropertyTextDecorationContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.TEXT_DECORATION,
      ctx.PROPERTY_TEXT_DECORATION_VALUE().getText()
    );
    return null;
  }

  @Override
  public Object visitPropertyTextTransform(RainParser.PropertyTextTransformContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.TEXT_TRANSFORM,
      ctx.PROPERTY_TEXT_TRANSFORM_VALUE().getText()
    );
    return null;
  }

  @Override
  public Object visitPropertyTextAlign(RainParser.PropertyTextAlignContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.TEXT_ALIGN, ctx.PROPERTY_TEXT_ALIGN_VALUE().getText());
    return null;
  }

  @Override
  public Object visitPropertyTextColor(RainParser.PropertyTextColorContext ctx) {
    this.currentShapeProperties.setProperty(ShapeProperty.TEXT_COLOR, ctx.COLOR().getText());
    return null;
  }

  @Override
  public Object visitPropertyFontSize(RainParser.PropertyFontSizeContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.FONT_SIZE,
      RainUtils.extractPxNumber(ctx.NUMBER_PX().getText())
    );
    return null;
  }

  @Override
  public Object visitPropertyText(RainParser.PropertyTextContext ctx) {
    this.currentShapeProperties.setProperty(
      ShapeProperty.TEXT,
      RainUtils.extractTextValue(ctx.PROPERTY_TEXT_VALUE().getText())
    );
    return null;
  }

  @Override
  public Object visitView(RainParser.ViewContext ctx) {
    return new View(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      Float.parseFloat(RainUtils.extractPxNumber(ctx.propertyHeight().NUMBER_PX().getText())),
      Float.parseFloat(RainUtils.extractPxNumber(ctx.propertyWidth().NUMBER_PX().getText())),
      null,
      ctx.ROOT() != null,
      ctx.componentElement().stream().map(componentElementContext -> (Element) visit(componentElementContext)).toList()
    );
  }

  @Override
  public Object visitComponent(RainParser.ComponentContext ctx) {
    return new Component(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      (Position) visit(ctx.position()),
      null,
      (ctx.codeModules() == null)
        ? null
        : RainUtils.extractCodeModuleCode(ctx.codeModules().CODE().getText()),
      ctx.element().stream().map(elementContext -> (Element) visit(elementContext)).toList()
    );
  }

  @Override
  public Object visitComponentReference(RainParser.ComponentReferenceContext ctx) {
    return new Component(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID(0).getText(),
      (Position) visit(ctx.position()),
      new BlueprintComponentReference(ctx.ID(1).getText(), new ArrayList<>(), new ArrayList<>()),
      null,
      new ArrayList<>()
    );
  }

  @Override
  public Object visitServer(RainParser.ServerContext ctx) {
    this.currentServerName = ctx.name.getText();

    Server server = new Server(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.name.getText(),
      (ctx.hostname != null) ? ctx.hostname.getText() : ctx.ipAddress.getText(),
      Integer.parseInt(ctx.NUMBER().getText()),
      ctx.route().stream().map(routeContext -> (Route) visit(routeContext)).toList()
    );

    this.currentServerName = null;
    return server;
  }

  @Override
  public Object visitRoute(RainParser.RouteContext ctx) {
    RouteMethod routeMethod = RouteMethod.GET;
    if (ctx.ROUTE_POST() != null) {
      routeMethod = RouteMethod.POST;
    }

    if (ctx.ROUTE_UPDATE() != null) {
      routeMethod = RouteMethod.UPDATE;
    }

    if (ctx.ROUTE_DELETE() != null) {
      routeMethod = RouteMethod.DELETE;
    }

    ThunderAnalyzer thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.TYPE_CHECKER, ThunderAnalyzerMode.SERVER);
    String code = ctx.codeModules().CODE().getText();

    ServiceProvider.provide(CompilerErrorService.class).setLineOffset(ctx.codeModules().start.getLine() - 1);
    ServiceProvider.provide(SymbolTableService.class).initializeContextSymbolTable(new ServerRouteSymbolTableIdentifier(
      this.currentServerName,
      ctx.ID().getText()
    ));
    Optional<CodeModules> codeModulesOptional = thunderAnalyzer.analyze(code.substring(1, code.length() - 2));
    ServiceProvider.provide(CompilerErrorService.class).resetLineOffset();



    Optional<BootsSpecification> bootsSpecificationOptional = Optional.empty();
    if (ctx.bootsSpecification() != null) {
      BootsSpecificationAnalyzer bootsSpecificationAnalyzer = new BootsSpecificationAnalyzer(CompilerPhase.TYPE_CHECKER);
      String bootsSpecificationCode = ctx.bootsSpecification().CODE().getText();
      bootsSpecificationOptional = bootsSpecificationAnalyzer.analyze(bootsSpecificationCode.substring(1, bootsSpecificationCode.length() - 2));
    }

    Optional<UmbrellaSpecifications> umbrellaSpecificationsOptional = Optional.empty();
    if (ctx.umbrellaSpecification() != null) {
      UmbrellaSpecificationAnalyzer umbrellaSpecificationAnalyzer = new UmbrellaSpecificationAnalyzer(CompilerPhase.TYPE_CHECKER);
      String umbrellaSpecificationCode = ctx.umbrellaSpecification().CODE().getText();
      umbrellaSpecificationsOptional = umbrellaSpecificationAnalyzer.analyze(umbrellaSpecificationCode.substring(1, umbrellaSpecificationCode.length() - 2));
    }

    ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ClientSymbolTableIdentifier());

    return new Route(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText(),
      routeMethod,
      codeModulesOptional.orElse(null),
      bootsSpecificationOptional.orElse(null),
      umbrellaSpecificationsOptional.orElse(null)
    );
  }
}
