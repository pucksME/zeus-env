package zeus.zeuscompiler.umbrellaspecification.compiler.visitors;

import zeus.zeuscompiler.grammars.UmbrellaSpecificationBaseVisitor;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationParser;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.*;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.IdentifierFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.ArithmeticBinaryFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.ArithmeticBinaryFormulaType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.CompareBinaryFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.CompareBinaryFormulaType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary.AccessFormula;

import java.util.HashMap;

public class UmbrellaSpecificationVisitor extends UmbrellaSpecificationBaseVisitor<Object> {
  UmbrellaSpecifications umbrellaSpecifications;

  @Override
  public UmbrellaSpecifications visitSpecifications(UmbrellaSpecificationParser.SpecificationsContext ctx) {
    this.umbrellaSpecifications = new UmbrellaSpecifications(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      new HashMap<>()
    );

    ctx.formulaAssignment().forEach(this::visitFormulaAssignment);
    ctx.contextAssignment().forEach(this::visitContextAssignment);
    ctx.actionAssignment().forEach(this::visitActionAssignment);

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
  public Object visitActionAssignment(UmbrellaSpecificationParser.ActionAssignmentContext ctx) {
    this.umbrellaSpecifications.setUmbrellaSpecificationAction(
      ctx.ID().getText(),
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (ctx.ACTION_ALLOW() != null) ? Action.ALLOW : ((ctx.ACTION_BLOCK() != null) ? Action.BLOCK : Action.LOG)
    );
    return null;
  }

  @Override
  public Object visitAccessFormula(UmbrellaSpecificationParser.AccessFormulaContext ctx) {
    return new AccessFormula(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      (Formula) this.visit(ctx.formula())
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
}
