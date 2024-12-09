package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.LiteralFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.binary.*;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary.AccessFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary.LogicalNotFormula;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.unary.TemporalUnaryFormula;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.List;

public class UmbrellaSpecification extends Node {
  String id;
  Formula formula;
  Context context;
  Action action;

  public UmbrellaSpecification(int line, int linePosition, String id) {
    super(line, linePosition);
    this.id = id;
  }

  @Override
  public void check() {
    this.formula.check();
  }

  // Monitor generation algorithm adapted from
  // Havelund, Klaus, and Grigore Roşu. "Efficient monitoring of safety properties."
  // International Journal on Software Tools for Technology Transfer 6 (2004): 158-173.
  public String translate() {
    ArrayList<String> code = new ArrayList<>();
    code.add("import java.util.HashMap;");
    code.add("import java.util.stream.Stream;");
    code.add("");
    code.add(String.format("public class Specification%s extends Specification {", this.id));
    code.add(CompilerUtils.buildLinePadding(1) + "HashMap<String, String> state;");
    code.add(CompilerUtils.buildLinePadding(1) + "boolean[] pre;");
    code.add(CompilerUtils.buildLinePadding(1) + "boolean[] now;");
    code.add("");
    code.add(CompilerUtils.buildLinePadding(1) + String.format("public Specification%s() {", this.id));
    code.add(CompilerUtils.buildLinePadding(2) + "this.state = new HashMap<>();");
    List<Formula> subFormulas = this.formula.getSubFormulas();
    code.add(CompilerUtils.buildLinePadding(2) + String.format("this.pre = new boolean[%s]", subFormulas.size()));
    code.add(CompilerUtils.buildLinePadding(2) + String.format("this.now = new boolean[%s]", subFormulas.size()));
    code.add(CompilerUtils.buildLinePadding(1) + "}");
    code.add("");
    code.add(CompilerUtils.buildLinePadding(1) + "@Overwrite");
    code.add(CompilerUtils.buildLinePadding(1) + "public boolean verify(Request request, SpecificationIdentifier specificationIdentifier) {");
    code.add(CompilerUtils.buildLinePadding(2) + "List<Request> requests = Stream.concat(SpecificationService.getRequests(specificationIdentifier).stream(), Stream.of(request)).toList();");
    code.add(CompilerUtils.buildLinePadding(2) + "SpecificationService.addRequest(request, specificationIdentifier);");
    code.add(CompilerUtils.buildLinePadding(2) + "this.state = requests.get(0).getVariables();");

    for (int i = 0; i < subFormulas.size(); i++) {
      Formula subFormula = subFormulas.get(i);

      if (subFormula instanceof AccessFormula) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = this.state[%s]",
          i,
          String.join(".", ((AccessFormula) subFormula).buildIdentifiers())
        ));
        continue;
      }

      if (subFormula instanceof LiteralFormula) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = %s",
          i,
          ((LiteralFormula) subFormula).getValue()
        ));
        continue;
      }

      if (subFormula instanceof LogicalNotFormula) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = !pre[%s]",
          i,
          subFormulas.indexOf(((LogicalNotFormula) subFormula).getFormula())
        ));
        continue;
      }

      if (subFormula instanceof ArithmeticBinaryFormula) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = pre[%s] %s pre[%s]",
          i,
          subFormulas.indexOf(((ArithmeticBinaryFormula) subFormula).getLeftFormula()),
          switch (((ArithmeticBinaryFormula) subFormula).getArithmeticBinaryFormulaType()) {
            case ADD -> "+";
            case SUBTRACT -> "-";
            case MULTIPLY -> "*";
            case DIVIDE -> "/";
          },
          subFormulas.indexOf(((ArithmeticBinaryFormula) subFormula).getRightFormula())
        ));
        continue;
      }

      if (subFormula instanceof CompareBinaryFormula) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = pre[%s] %s pre[%s]",
          i,
          subFormulas.indexOf(((CompareBinaryFormula) subFormula).getLeftFormula()),
          switch (((CompareBinaryFormula) subFormula).getCompareBinaryFormulaType()) {
            case EQUAL -> "==";
            case NOT_EQUAL -> "!=";
            case GREATER_THAN -> ">";
            case LESS_THAN -> "<";
            case GREATER_EQUAL_THAN -> ">=";
            case LESS_EQUAL_THAN -> "<=";
          },
          subFormulas.indexOf(((CompareBinaryFormula) subFormula).getRightFormula())
        ));
        continue;
      }

      if (subFormula instanceof LogicalBinaryFormula) {
        boolean isImplication = ((LogicalBinaryFormula) subFormula).getLogicalBinaryFormulaType() ==
          LogicalBinaryFormulaType.IMPLICATION;

        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = %spre[%s] %s pre[%s]",
          i,
          (isImplication) ? "!" : "",
          subFormulas.indexOf(((LogicalBinaryFormula) subFormula).getLeftFormula()),
          (isImplication) ? "||" : switch (((LogicalBinaryFormula) subFormula).getLogicalBinaryFormulaType()) {
            case AND -> "&&";
            case OR -> "||";
            case IMPLICATION -> throw new RuntimeException("Could not generate monitor: unhandled implication");
          },
          subFormulas.indexOf(((LogicalBinaryFormula) subFormula).getRightFormula())
        ));
        continue;
      }

      if (subFormula instanceof TemporalUnaryFormula) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = pre[%s]",
          i,
          subFormulas.indexOf(((TemporalUnaryFormula) subFormula).getFormula())
        ));
      }

      if (subFormula instanceof TemporalBinaryFormula &&
        ((TemporalBinaryFormula) subFormula).getTemporalBinaryFormulaType() == TemporalBinaryFormulaType.SINCE) {
        code.add(CompilerUtils.buildLinePadding(2) + String.format(
          "pre[%s] = pre[%s]",
          i,
          subFormulas.indexOf(((TemporalBinaryFormula) subFormula).getRightFormula())
        ));
      }
    }

    code.add(CompilerUtils.buildLinePadding(1) + "}");
    code.add("}");

    return String.join("\n", code);
  }

  public void setFormula(Formula formula) {
    this.formula = formula;
  }

  public void setContext(Context context) {
    this.context = context;
  }

  public void setAction(Action action) {
    this.action = action;
  }
}
