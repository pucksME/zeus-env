package zeus.zeuscompiler.umbrellaspecification.compiler.visitors;

import zeus.zeuscompiler.grammars.UmbrellaSpecificationBaseVisitor;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationParser;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.*;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.IdentifierFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.LiteralFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.LiteralFormulaType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.*;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary.*;

import java.util.HashMap;
import java.util.stream.Collectors;

public class UmbrellaSpecificationVisitor extends UmbrellaSpecificationBaseVisitor<Object> {
  UmbrellaSpecifications umbrellaSpecifications;

  @Override
  public UmbrellaSpecifications visitSpecifications(UmbrellaSpecificationParser.SpecificationsContext ctx) {
    this.umbrellaSpecifications = new UmbrellaSpecifications(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      new HashMap<>()
    );

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerRouteSymbolTable.class).setUmbrellaSpecifications(this.umbrellaSpecifications);

    ctx.formulaAssignment().forEach(this::visitFormulaAssignment);
    ctx.contextAssignment().forEach(this::visitContextAssignment);
    ctx.actionsAssignment().forEach(this::visitActionsAssignment);

    return this.umbrellaSpecifications;
  }

  @Override
  public Object visitFormulaAssignment(UmbrellaSpecificationParser.FormulaAssignmentContext ctx) {
    this.umbrellaSpecifications.setUmbrellaSpecificationFormula(
      ctx.ID().getText(),
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula())
    );
    return null;
  }

  @Override
  public Object visitContextAssignment(UmbrellaSpecificationParser.ContextAssignmentContext ctx) {
    this.umbrellaSpecifications.setUmbrellaSpecificationContext(
      ctx.ID().getText(),
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (ctx.CONTEXT_GLOBAL() != null) ? Context.GLOBAL : Context.IP
    );
    return null;
  }

  @Override
  public Object visitActionsAssignment(UmbrellaSpecificationParser.ActionsAssignmentContext ctx) {
    this.umbrellaSpecifications.setUmbrellaSpecificationAction(
      ctx.ID().getText(),
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.actions().action().stream()
        .map(actionContext -> (actionContext.ACTION_BLOCK() != null) ? Action.BLOCK : Action.LOG)
        .collect(Collectors.toSet())
    );
    return null;
  }

  @Override
  public Object visitAccessFormula(UmbrellaSpecificationParser.AccessFormulaContext ctx) {
    return new AccessFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula()),
      ctx.ID().getText()
    );
  }

  @Override
  public Object visitAccessListFormula(UmbrellaSpecificationParser.AccessListFormulaContext ctx) {
    return new AccessListFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula(0)),
      (ctx.formula().size() > 1) ? (Formula) this.visit(ctx.formula(1)) : null,
      ctx.ID().getText()
    );
  }

  @Override
  public Object visitIdentifierFormula(UmbrellaSpecificationParser.IdentifierFormulaContext ctx) {
    return new IdentifierFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.ID().getText()
    );
  }

  @Override
  public Object visitCompareBinaryFormula(UmbrellaSpecificationParser.CompareBinaryFormulaContext ctx) {
    return new CompareBinaryFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula().get(0)),
      (Formula) this.visit(ctx.formula().get(1)),
      switch (ctx.operator.getType()) {
        case UmbrellaSpecificationParser.OPERATOR_EQUAL -> CompareBinaryFormulaType.EQUAL;
        case UmbrellaSpecificationParser.OPERATOR_NOT_EQUAL -> CompareBinaryFormulaType.NOT_EQUAL;
        case UmbrellaSpecificationParser.OPERATOR_GREATER_THAN -> CompareBinaryFormulaType.GREATER_THAN;
        case UmbrellaSpecificationParser.OPERATOR_LESS_THAN -> CompareBinaryFormulaType.LESS_THAN;
        case UmbrellaSpecificationParser.OPERATOR_GREATER_EQUAL_THAN -> CompareBinaryFormulaType.GREATER_EQUAL_THAN;
        case UmbrellaSpecificationParser.OPERATOR_LESS_EQUAL_THAN -> CompareBinaryFormulaType.LESS_EQUAL_THAN;
        default -> throw new RuntimeException("Unsupported compare binary formula type");
      }
    );
  }

  @Override
  public Object visitArithmeticBinaryFormula(UmbrellaSpecificationParser.ArithmeticBinaryFormulaContext ctx) {
    return new ArithmeticBinaryFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula().get(0)),
      (Formula) this.visit(ctx.formula().get(1)),
      switch (ctx.operator.getType()) {
        case UmbrellaSpecificationParser.OPERATOR_ADD -> ArithmeticBinaryFormulaType.ADD;
        case UmbrellaSpecificationParser.OPERATOR_SUBTRACT -> ArithmeticBinaryFormulaType.SUBTRACT;
        case UmbrellaSpecificationParser.OPERATOR_MULTIPLY -> ArithmeticBinaryFormulaType.MULTIPLY;
        case UmbrellaSpecificationParser.OPERATOR_DIVIDE -> ArithmeticBinaryFormulaType.DIVIDE;
        default -> throw new RuntimeException("Unsupported arithmetic binary formula type");
      }
    );
  }

  @Override
  public Object visitLogicalBinaryFormula(UmbrellaSpecificationParser.LogicalBinaryFormulaContext ctx) {
    return new LogicalBinaryFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula().get(0)),
      (Formula) this.visit(ctx.formula().get(1)),
      switch (ctx.operator.getType()) {
        case UmbrellaSpecificationParser.OPERATOR_AND -> LogicalBinaryFormulaType.AND;
        case UmbrellaSpecificationParser.OPERATOR_OR -> LogicalBinaryFormulaType.OR;
        case UmbrellaSpecificationParser.OPERATOR_IMPLICATION -> LogicalBinaryFormulaType.IMPLICATION;
        default -> throw new RuntimeException("Unsupported logical binary formula type");
      }
    );
  }

  @Override
  public Object visitTemporalBinaryFormula(UmbrellaSpecificationParser.TemporalBinaryFormulaContext ctx) {
    return new TemporalBinaryFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula().get(0)),
      (Formula) this.visit(ctx.formula().get(1)),
      TemporalBinaryFormulaType.SINCE
    );
  }

  @Override
  public Object visitLogicalNotFormula(UmbrellaSpecificationParser.LogicalNotFormulaContext ctx) {
    return new LogicalNotFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula())
    );
  }

  @Override
  public Object visitArithmeticNegativeFormula(UmbrellaSpecificationParser.ArithmeticNegativeFormulaContext ctx) {
    return new ArithmeticNegativeFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula())
    );
  }

  @Override
  public Object visitTemporalUnaryFormula(UmbrellaSpecificationParser.TemporalUnaryFormulaContext ctx) {
    return new TemporalUnaryFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula()),
      switch (ctx.operator.getType()) {
        case UmbrellaSpecificationParser.OPERATOR_YEASTERDAY -> TemporalUnaryFormulaType.YESTERDAY;
        case UmbrellaSpecificationParser.OPERATOR_ONCE -> TemporalUnaryFormulaType.ONCE;
        case UmbrellaSpecificationParser.OPERATOR_HISTORICALLY -> TemporalUnaryFormulaType.HISTORICALLY;
        default -> throw new RuntimeException("Unsupported temporal unary formula type");
      }
    );
  }

  @Override
  public Object visitLiteralFormula(UmbrellaSpecificationParser.LiteralFormulaContext ctx) {
    return new LiteralFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.literal.getText(),
      switch (ctx.literal.getType()) {
        case UmbrellaSpecificationParser.LITERAL_BOOLEAN -> LiteralFormulaType.BOOLEAN;
        case UmbrellaSpecificationParser.LITERAL_INT -> LiteralFormulaType.INT;
        case UmbrellaSpecificationParser.LITERAL_FLOAT -> LiteralFormulaType.FLOAT;
        case UmbrellaSpecificationParser.LITERAL_STRING -> LiteralFormulaType.STRING;
        default -> throw new RuntimeException("Unsupported literal formula type");
      }
    );
  }

  @Override
  public Object visitParenthesisFormula(UmbrellaSpecificationParser.ParenthesisFormulaContext ctx) {
    return this.visit(ctx.formula());
  }
}
