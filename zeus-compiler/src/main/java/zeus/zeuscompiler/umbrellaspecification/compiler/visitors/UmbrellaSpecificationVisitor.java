package zeus.zeuscompiler.umbrellaspecification.compiler.visitors;

import zeus.zeuscompiler.grammars.UmbrellaSpecificationBaseVisitor;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationParser;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.*;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.binary.CompareBinaryFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.binary.CompareBinaryFormulaType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.unary.AccessFormula;

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
        default -> throw new RuntimeException("Unsupported compare binary formula type");
      }
    );
  }
}
