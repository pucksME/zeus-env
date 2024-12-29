package zeus.zeuscompiler.thunder.compiler.visitors;

import org.antlr.v4.runtime.Token;
import zeus.zeuscompiler.grammars.ThunderBaseVisitor;
import zeus.zeuscompiler.grammars.ThunderParser;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzerMode;
import zeus.zeuscompiler.symboltable.VariableType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.UnknownLiteralException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.binary.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.ternary.IfElseExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.ternary.TernaryExpressionType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.unary.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ThunderVisitor extends ThunderBaseVisitor<Object> {
  ThunderAnalyzerMode thunderAnalyzerMode;

  public ThunderVisitor(ThunderAnalyzerMode thunderAnalyzerMode) {
    this.thunderAnalyzerMode = thunderAnalyzerMode;
  }

  @Override
  public CodeModules visitCodeModules(ThunderParser.CodeModulesContext ctx) {
    CodeModules codeModules = new CodeModules(
      ctx.clientCodeModule().stream().map(
        clientCodeModuleContext -> (ClientCodeModule) visit(clientCodeModuleContext)
      ).collect(Collectors.toList()),
      ctx.instanceCodeModule().stream().map(
        instanceCodeModuleContext -> (InstanceCodeModule) visit(instanceCodeModuleContext)
      ).collect(Collectors.toList())
    );

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).setCodeModules(codeModules);

    return codeModules;
  }

  @Override
  public ClientCodeModule visitClientCodeModule(ThunderParser.ClientCodeModuleContext ctx) {
    ClientCodeModule clientCodeModule = switch (ctx.ID().getText()) {
      case "request" -> new RequestCodeModule(
        ctx.KEYWORD_CODE_MODULE().getSymbol().getLine(),
        ctx.KEYWORD_CODE_MODULE().getSymbol().getCharPositionInLine(),
        ctx.ID().getText(),
        ctx.DESCRIPTION().getText()
      );

      case "response" -> new ResponseCodeModule(
        ctx.KEYWORD_CODE_MODULE().getSymbol().getLine(),
        ctx.KEYWORD_CODE_MODULE().getSymbol().getCharPositionInLine(),
        ctx.ID().getText(),
        ctx.DESCRIPTION().getText()
      );

      default -> new ClientCodeModule(
        ctx.KEYWORD_CODE_MODULE().getSymbol().getLine(),
        ctx.KEYWORD_CODE_MODULE().getSymbol().getCharPositionInLine(),
        ctx.ID().getText(),
        ctx.DESCRIPTION().getText()
      );
    };

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).setCurrentCodeModule(clientCodeModule);

    clientCodeModule.setHead((Head) visit(ctx.head()));
    clientCodeModule.setBody((Body) visit(ctx.body()));

    return clientCodeModule;
  }

  @Override
  public Head visitHead(ThunderParser.HeadContext ctx) {
    return new Head(
      ctx.input().stream().map(inputContext -> (Input) visit(inputContext)).collect(Collectors.toList()),
      ctx.output().stream().map(outputContext -> (Output) visit(outputContext)).collect(Collectors.toList()),
      ctx.config().stream().map(configContext -> (Config) visit(configContext)).collect(Collectors.toList())
    );
  }

  @Override
  public Input visitInput(ThunderParser.InputContext ctx) {
    Input input = new Input(
      ctx.KEYWORD_INPUT().getSymbol().getLine(),
      ctx.KEYWORD_INPUT().getSymbol().getCharPositionInLine(),
      ctx.ID().getText(),
      (Type) visit(ctx.type())
    );

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).addVariable(
      input.getId(),
      input.getType(),
      VariableType.INPUT,
      input.getLine(),
      input.getLinePosition()
    );

    return input;
  }

  @Override
  public Output visitOutput(ThunderParser.OutputContext ctx) {
    Output output = new Output(
      ctx.KEYWORD_OUTPUT().getSymbol().getLine(),
      ctx.KEYWORD_OUTPUT().getSymbol().getCharPositionInLine(),
      ctx.declaration().ID().getText(),
      (Type) visit(ctx.declaration().type()),
      (ctx.declaration().OPERATOR_ASSIGNMENT() != null) ? (Expression) visit(ctx.declaration().expression()) : null
    );

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).addVariable(
      output.getId(),
      output.getType(),
      VariableType.OUTPUT,
      output.getLine(),
      output.getLinePosition()
    );

    return output;
  }

  @Override
  public Config visitConfig(ThunderParser.ConfigContext ctx) {
    Type type = (Type) visit(ctx.declaration().type());
    int line = ctx.KEYWORD_CONFIG().getSymbol().getLine();
    int linePosition = ctx.KEYWORD_CONFIG().getSymbol().getCharPositionInLine();
    String id = ctx.declaration().ID().getText();
    Expression expression = (ctx.declaration().OPERATOR_ASSIGNMENT() != null)
      ? (Expression) visit(ctx.declaration().expression())
      : null;

    Config config = (type instanceof PrimitiveType)
      ? new InputConfig(line, linePosition, id, type, expression)
      : new SelectionConfig(line, linePosition, id, type, expression);

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).addVariable(
      config.getId(),
      config.getType(),
      VariableType.CONFIG,
      config.getLine(),
      config.getLinePosition()
    );

    return config;
  }

  @Override
  public DeclarationTypeStatement visitDeclarationTypeStatement(ThunderParser.DeclarationTypeStatementContext ctx) {
    String typeId = ctx.ID().getText();
    Type type = (Type) visit(ctx.type());

    if (ctx.KEYWORD_PUBLIC() != null) {
      ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(SymbolTable.class).addPublicType(typeId, type);
    } else {
      ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(SymbolTable.class).addPrivateType(typeId, type);
    }

    Token startToken = (ctx.KEYWORD_PUBLIC() != null)
      ? ctx.KEYWORD_PUBLIC().getSymbol()
      : ctx.KEYWORD_TYPE().getSymbol();

    return new DeclarationTypeStatement(
      startToken.getLine(),
      startToken.getCharPositionInLine(),
      typeId,
      ctx.KEYWORD_PUBLIC() != null
    );
  }

  @Override
  public Type visitType(ThunderParser.TypeContext ctx) {
    if (ctx.PRIMITIVE_TYPE() != null) {
      return new PrimitiveType(
        ctx.PRIMITIVE_TYPE().getSymbol().getLine(),
        ctx.PRIMITIVE_TYPE().getSymbol().getCharPositionInLine(),
        ctx.PRIMITIVE_TYPE().getText()
      );
    }

    if (ctx.ID() != null) {
      return new IdType(
        ctx.ID().getSymbol().getLine(),
        ctx.ID().getSymbol().getCharPositionInLine(),
        ctx.ID().getText()
      );
    }

    return (Type) visitChildren(ctx);
  }

  @Override
  public ListType visitTypeList(ThunderParser.TypeListContext ctx) {
    return new ListType(
      ctx.LIST_START().getSymbol().getLine(),
      ctx.LIST_START().getSymbol().getCharPositionInLine(),
      (Type) visit(ctx.type()),
      (ctx.OPERATOR_SIZE() != null) ? Integer.parseInt(ctx.LITERAL_INT().getText()) : -1
    );
  }

  @Override
  public MapType visitTypeMap(ThunderParser.TypeMapContext ctx) {
    return new MapType(
      ctx.LIST_START().getSymbol().getLine(),
      ctx.LIST_START().getSymbol().getCharPositionInLine(),
      (Type) visit(ctx.type(0)),
      (Type) visit(ctx.type(1))
    );
  }

  @Override
  public Type visitTypeObjectItem(ThunderParser.TypeObjectItemContext ctx) {
    return (Type) visit(ctx.type());
  }

  @Override
  public ObjectType visitTypeObject(ThunderParser.TypeObjectContext ctx) {
    Map<String, Type> propertyTypes = new HashMap<>();

    for (ThunderParser.TypeObjectItemContext typeObjectItemContext : ctx.typeObjectItem()) {
      propertyTypes.put(typeObjectItemContext.ID().getText(), (Type) visit(typeObjectItemContext.type()));
    }

    return new ObjectType(
      ctx.BLOCK_START().getSymbol().getLine(),
      ctx.BLOCK_START().getSymbol().getCharPositionInLine(),
      propertyTypes
    );
  }

  // handled directly in other visitor methods
  @Override
  public Object visitDeclaration(ThunderParser.DeclarationContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public Body visitBody(ThunderParser.BodyContext ctx) {
    return new Body((ctx.children == null)
      ? new ArrayList<>()
      : ctx.children.stream().map(child -> (BodyComponent) visit(child)).collect(Collectors.toList())
    );
  }

  @Override
  public Statement visitStatement(ThunderParser.StatementContext ctx) {
    return (Statement) visitChildren(ctx);
  }

  @Override
  public DeclarationVariableStatement visitDeclarationVariableStatement(ThunderParser.DeclarationVariableStatementContext ctx) {
    String variableId = ctx.declaration().ID().getText();
    int variableLine = ctx.KEYWORD_DECLARATION().getSymbol().getLine();
    int variableLinePosition = ctx.KEYWORD_DECLARATION().getSymbol().getCharPositionInLine();
    Type type = (Type) visit(ctx.declaration().type());

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).addVariable(variableId, type, VariableType.VARIABLE, variableLine, variableLinePosition);

    return new DeclarationVariableStatement(
      variableLine,
      variableLinePosition,
      variableId,
      type,
      (ctx.declaration().OPERATOR_ASSIGNMENT() != null) ? (Expression) visit(ctx.declaration().expression()) : null
    );
  }

  @Override
  public AssignmentStatement visitAssignmentStatement(ThunderParser.AssignmentStatementContext ctx) {
    return new AssignmentStatement(
      ctx.ID().getSymbol().getLine(),
      ctx.ID().getSymbol().getCharPositionInLine(),
      ctx.ID().getText(),
      (Expression) visit(ctx.expression())
    );
  }

  @Override
  public AccessWriteStatement visitAccessWriteStatement(ThunderParser.AccessWriteStatementContext ctx) {
    return new AccessWriteStatement(
      ctx.expression(0).getStart().getLine(),
      ctx.expression(0).getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      (Expression) visit(ctx.expression(2))
    );
  }

  @Override
  public AccessWriteObjectStatement visitAccessWriteObjectStatement(ThunderParser.AccessWriteObjectStatementContext ctx) {
    return new AccessWriteObjectStatement(
      ctx.expression(0).getStart().getLine(),
      ctx.expression(0).getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      ctx.ID().getText(),
      (Expression) visit(ctx.expression(1))
    );
  }

  @Override
  public IfStatement visitIfStatement(ThunderParser.IfStatementContext ctx) {
    return new IfStatement(
      ctx.KEYWORD_IF().getSymbol().getLine(),
      ctx.KEYWORD_IF().getSymbol().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      (Body) visit(ctx.body(0)),
      (ctx.KEYWORD_ELSE() != null) ? (Body) visit(ctx.body(1)) : null
    );
  }

  @Override
  public WhileStatement visitWhileStatement(ThunderParser.WhileStatementContext ctx) {
    return new WhileStatement(
      ctx.KEYWORD_WHILE().getSymbol().getLine(),
      ctx.KEYWORD_WHILE().getSymbol().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      (Body) visit(ctx.body())
    );
  }

  @Override
  public IdentifierExpression visitIdentifierExpression(ThunderParser.IdentifierExpressionContext ctx) {
    return new IdentifierExpression(
      ctx.ID().getSymbol().getLine(),
      ctx.ID().getSymbol().getCharPositionInLine(),
      ctx.ID().getText()
    );
  }

  @Override
  public MapExpression visitMapExpression(ThunderParser.MapExpressionContext ctx) {
    return new MapExpression(
      ctx.expressionMap().LIST_START().getSymbol().getLine(),
      ctx.expressionMap().LIST_START().getSymbol().getCharPositionInLine(),
      ctx.expressionMap().expressionMapItem().stream().map(
        expressionMapItemContext -> (MapItem) visit(expressionMapItemContext)
      ).collect(Collectors.toList())
    );
  }

  @Override
  public MapItem visitExpressionMapItem(ThunderParser.ExpressionMapItemContext ctx) {
    return new MapItem((Expression) visit(ctx.expression(0)), (Expression) visit(ctx.expression(1)));
  }

  // handled directly in other visitor methods
  @Override
  public Object visitExpressionMap(ThunderParser.ExpressionMapContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public LiteralExpression visitLiteralExpression(ThunderParser.LiteralExpressionContext ctx) {
    if (ctx.literal().LITERAL_INT() != null) {
      return new LiteralExpression(
        ctx.literal().LITERAL_INT().getSymbol().getLine(),
        ctx.literal().LITERAL_INT().getSymbol().getCharPositionInLine(),
        ctx.literal().LITERAL_INT().getText(),
        LiteralType.INT
      );
    }

    if (ctx.literal().LITERAL_FLOAT() != null) {
      return new LiteralExpression(
        ctx.literal().LITERAL_FLOAT().getSymbol().getLine(),
        ctx.literal().LITERAL_FLOAT().getSymbol().getCharPositionInLine(),
        ctx.literal().LITERAL_FLOAT().getText(),
        LiteralType.FLOAT
      );
    }

    if (ctx.literal().LITERAL_STRING() != null) {
      String value = ctx.literal().LITERAL_STRING().getText();
      assert value.length() >= 2;

      return new LiteralExpression(
        ctx.literal().LITERAL_STRING().getSymbol().getLine(),
        ctx.literal().LITERAL_STRING().getSymbol().getCharPositionInLine(),
        value.substring(1, value.length() - 1),
        LiteralType.STRING
      );
    }

    if (ctx.literal().LITERAL_BOOLEAN() != null) {
      return new LiteralExpression(
        ctx.literal().LITERAL_BOOLEAN().getSymbol().getLine(),
        ctx.literal().LITERAL_BOOLEAN().getSymbol().getCharPositionInLine(),
        ctx.literal().LITERAL_BOOLEAN().getText(),
        LiteralType.BOOLEAN
      );
    }

    if (ctx.literal().LITERAL_NULL() != null) {
      return new LiteralExpression(
        ctx.literal().LITERAL_NULL().getSymbol().getLine(),
        ctx.literal().LITERAL_NULL().getSymbol().getCharPositionInLine(),
        ctx.literal().LITERAL_NULL().getText(),
        LiteralType.NULL
      );
    }

    throw new UnknownLiteralException();
  }

  // handled directly in other visitor methods
  @Override
  public Object visitLiteral(ThunderParser.LiteralContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public ListExpression visitListExpression(ThunderParser.ListExpressionContext ctx) {
    return new ListExpression(
      ctx.expressionList().LIST_START().getSymbol().getLine(),
      ctx.expressionList().LIST_START().getSymbol().getCharPositionInLine(),
      ctx.expressionList().expression().stream().map(
        expressionContext -> (Expression) visit(expressionContext)
      ).collect(Collectors.toList())
    );
  }

  // handled directly in other visitor methods
  @Override
  public Object visitExpressionList(ThunderParser.ExpressionListContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public GroupExpression visitGroupExpression(ThunderParser.GroupExpressionContext ctx) {
    return new GroupExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      UnaryExpressionType.GROUP
    );
  }

  @Override
  public ObjectReadAccessExpression visitObjectReadAccessExpression(ThunderParser.ObjectReadAccessExpressionContext ctx) {
    return new ObjectReadAccessExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      ctx.ID().getText(),
      UnaryExpressionType.OBJECT_READ_ACCESS
    );
  }

  @Override
  public CastExpression visitCastExpression(ThunderParser.CastExpressionContext ctx) {
    return new CastExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Type) visit(ctx.type()),
      (Expression) visit(ctx.expression()),
      UnaryExpressionType.CAST
    );
  }

  @Override
  public ObjectExpression visitObjectExpression(ThunderParser.ObjectExpressionContext ctx) {
    return new ObjectExpression(
      ctx.expressionObject().BLOCK_START().getSymbol().getLine(),
      ctx.expressionObject().BLOCK_START().getSymbol().getCharPositionInLine(),
      ctx.expressionObject().expressionObjectItem().stream().map(
        expressionObjectItemContext -> (ObjectItem) visit(expressionObjectItemContext)
      ).collect(Collectors.toList())
    );
  }

  @Override
  public ObjectItem visitExpressionObjectItem(ThunderParser.ExpressionObjectItemContext ctx) {
    return new ObjectItem(ctx.ID().getText(), (Expression) visit(ctx.expression()));
  }

  // handled directly in other visitor methods
  @Override
  public Object visitExpressionObject(ThunderParser.ExpressionObjectContext ctx) {
    return visitChildren(ctx);
  }

  @Override
  public SizeExpression visitSizeExpression(ThunderParser.SizeExpressionContext ctx) {
    return new SizeExpression(
      ctx.OPERATOR_SIZE().getSymbol().getLine(),
      ctx.OPERATOR_SIZE().getSymbol().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      UnaryExpressionType.SIZE
    );
  }

  @Override
  public NegateExpression visitNegateExpression(ThunderParser.NegateExpressionContext ctx) {
    return new NegateExpression(
      ctx.OPERATOR_NEGATE().getSymbol().getLine(),
      ctx.OPERATOR_NEGATE().getSymbol().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      UnaryExpressionType.NEGATE
    );
  }

  @Override
  public NegativeExpression visitNegativeExpression(ThunderParser.NegativeExpressionContext ctx) {
    return new NegativeExpression(
      ctx.OPERATOR_SUBTRACT().getSymbol().getLine(),
      ctx.OPERATOR_SUBTRACT().getSymbol().getCharPositionInLine(),
      (Expression) visit(ctx.expression()),
      UnaryExpressionType.NEGATIVE
    );
  }

  @Override
  public PowerExpression visitPowerExpression(ThunderParser.PowerExpressionContext ctx) {
    return new PowerExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.POWER
    );
  }

  @Override
  public GreaterThanExpression visitGreaterThanExpression(ThunderParser.GreaterThanExpressionContext ctx) {
    return new GreaterThanExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.GREATER_THAN
    );
  }

  @Override
  public OrExpression visitOrExpression(ThunderParser.OrExpressionContext ctx) {
    return new OrExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.OR
    );
  }

  @Override
  public CompareExpression visitCompareExpression(ThunderParser.CompareExpressionContext ctx) {
    return new CompareExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.COMPARE
    );
  }

  @Override
  public SubtractExpression visitSubtractExpression(ThunderParser.SubtractExpressionContext ctx) {
    return new SubtractExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.SUBTRACT
    );
  }

  @Override
  public AndExpression visitAndExpression(ThunderParser.AndExpressionContext ctx) {
    return new AndExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.AND
    );
  }

  @Override
  public ReadAccessExpression visitReadAccessExpression(ThunderParser.ReadAccessExpressionContext ctx) {
    return new ReadAccessExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.READ_ACCESS
    );
  }

  @Override
  public LessEqualThanExpression visitLessEqualThanExpression(ThunderParser.LessEqualThanExpressionContext ctx) {
    return new LessEqualThanExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.LESS_EQUAL_THAN
    );
  }

  @Override
  public MultiplyExpression visitMultiplyExpression(ThunderParser.MultiplyExpressionContext ctx) {
    return new MultiplyExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.MULTIPLY
    );
  }

  @Override
  public CompareNegatedExpression visitCompareNegatedExpression(ThunderParser.CompareNegatedExpressionContext ctx) {
    return new CompareNegatedExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.COMPARE_NEGATED
    );
  }

  @Override
  public GreaterEqualThanExpression visitGreaterEqualThanExpression(ThunderParser.GreaterEqualThanExpressionContext ctx) {
    return new GreaterEqualThanExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.GREATER_EQUAL_THAN
    );
  }

  @Override
  public DivideExpression visitDivideExpression(ThunderParser.DivideExpressionContext ctx) {
    return new DivideExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.DIVIDE
    );
  }

  @Override
  public AddExpression visitAddExpression(ThunderParser.AddExpressionContext ctx) {
    return new AddExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.ADD
    );
  }

  @Override
  public ModuloExpression visitModuloExpression(ThunderParser.ModuloExpressionContext ctx) {
    return new ModuloExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.MODULO
    );
  }

  @Override
  public LessThanExpression visitLessThanExpression(ThunderParser.LessThanExpressionContext ctx) {
    return new LessThanExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      BinaryExpressionType.LESS_THAN
    );
  }

  @Override
  public IfElseExpression visitIfElseExpression(ThunderParser.IfElseExpressionContext ctx) {
    return new IfElseExpression(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Expression) visit(ctx.expression(0)),
      (Expression) visit(ctx.expression(1)),
      (Expression) visit(ctx.expression(2)),
      TernaryExpressionType.IF_ELSE
    );
  }

  @Override
  public Object visitInstanceCodeModule(ThunderParser.InstanceCodeModuleContext ctx) {
    InstanceCodeModule instanceCodeModule = new InstanceCodeModule(
      ctx.KEYWORD_CODE_MODULE().getSymbol().getLine(),
      ctx.KEYWORD_CODE_MODULE().getSymbol().getCharPositionInLine(),
      ctx.ID().getText(),
      ctx.DESCRIPTION().getText()
    );

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(SymbolTable.class).setCurrentCodeModule(instanceCodeModule);

    instanceCodeModule.setBody((ctx.instanceBody() != null)
      ? (Body) visit(ctx.instanceBody())
      : new Body(new ArrayList<>()));

    return instanceCodeModule;
  }

  @Override
  public Body visitInstanceBody(ThunderParser.InstanceBodyContext ctx) {
    return new Body((ctx.children == null)
      ? new ArrayList<>()
      : ctx.children.stream().map(child -> (BodyComponent) visit(child)).collect(Collectors.toList())
    );
  }

  @Override
  public Statement visitInstanceStatement(ThunderParser.InstanceStatementContext ctx) {
    return (Statement) visitChildren(ctx);
  }

  @Override
  public ConnectionStatement visitConnectionStatement(ThunderParser.ConnectionStatementContext ctx) {
    ThunderParser.ExpressionCodeModulePortContext expressionCodeModuleInputContext = ctx.expressionCodeModulePort(1);
    ThunderParser.ExpressionCodeModulePortContext expressionCodeModuleOutputContext = ctx.expressionCodeModulePort(0);

    return new ConnectionStatement(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      new CodeModuleInputExpression(
        expressionCodeModuleInputContext.ID(0).getSymbol().getLine(),
        expressionCodeModuleInputContext.ID(0).getSymbol().getCharPositionInLine(),
        expressionCodeModuleInputContext.ID(0).getText(),
        expressionCodeModuleInputContext.ID(1).getText()
      ),
      new CodeModuleOutputExpression(
        expressionCodeModuleOutputContext.ID(0).getSymbol().getLine(),
        expressionCodeModuleOutputContext.ID(0).getSymbol().getCharPositionInLine(),
        expressionCodeModuleOutputContext.ID(0).getText(),
        expressionCodeModuleOutputContext.ID(1).getText()
      )
    );
  }
}
