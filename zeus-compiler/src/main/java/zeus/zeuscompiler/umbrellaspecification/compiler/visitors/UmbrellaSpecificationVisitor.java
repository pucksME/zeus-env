package zeus.zeuscompiler.umbrellaspecification.compiler.visitors;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationBaseVisitor;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationParser;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.*;

import java.util.HashMap;
import java.util.List;

public class UmbrellaSpecificationVisitor extends UmbrellaSpecificationBaseVisitor<Object> {
  UmbrellaSpecifications umbrellaSpecifications;
  List<CompilerError> compilerErrors;

  public UmbrellaSpecificationVisitor(List<CompilerError> compilerErrors) {
    this.compilerErrors = compilerErrors;
  }

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
}
