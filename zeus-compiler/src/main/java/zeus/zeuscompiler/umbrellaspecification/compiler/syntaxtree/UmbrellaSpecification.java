package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas.Formula;
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

  public String translateContext() {
    return switch (this.context) {
      case IP -> "Context.IP";
      case GLOBAL -> "Context.GLOBAL";
    };
  }

  public String translateAction() {
    return switch (this.action) {
      case LOG -> "Action.LOG";
      case ALLOW -> "Action.ALLOW";
      case BLOCK -> "Action.BLOCK";
    };
  }

  // Monitor generation algorithm adapted from
  // Havelund, Klaus, and Grigore Roşu. "Efficient monitoring of safety properties."
  // International Journal on Software Tools for Technology Transfer 6 (2004): 158-173.
  public String translate(String serverName, String routeId) {
    ArrayList<String> code = new ArrayList<>();
    code.add("package zeus.specification;");
    code.add("");
    code.add("import java.util.Map;");
    code.add("import java.util.HashMap;");
    code.add("import java.util.List;");
    code.add("import java.util.stream.Stream;");
    code.add("import zeus.SpecificationService;");
    code.add("import zeus.Request;");
    code.add("import zeus.SpecificationIdentifier;");
    code.add("");
    code.add(String.format("public class Specification%s%s%s extends Specification {", serverName, routeId, this.id));
    code.add(CompilerUtils.buildLinePadding(1) + "Map<String, String> state;");
    code.add(CompilerUtils.buildLinePadding(1) + "boolean[] pre;");
    code.add(CompilerUtils.buildLinePadding(1) + "boolean[] now;");
    code.add("");
    code.add(CompilerUtils.buildLinePadding(1) + String.format(
      "public Specification%s%s%s(Context context, Action action) {",
      serverName,
      routeId,
      this.id
    ));
    code.add(CompilerUtils.buildLinePadding(2) + "super(context, action);");
    code.add(CompilerUtils.buildLinePadding(2) + "this.state = new HashMap<>();");
    List<Formula> subFormulas = this.formula.getSubFormulas();
    code.add(CompilerUtils.buildLinePadding(2) + String.format("this.pre = new boolean[%s];", subFormulas.size()));
    code.add(CompilerUtils.buildLinePadding(2) + String.format("this.now = new boolean[%s];", subFormulas.size()));
    code.add(CompilerUtils.buildLinePadding(1) + "}");
    code.add("");
    code.add(CompilerUtils.buildLinePadding(1) + "@Override");
    code.add(CompilerUtils.buildLinePadding(1) + "public boolean verify(Request request, SpecificationIdentifier specificationIdentifier) {");
    code.add(CompilerUtils.buildLinePadding(2) + "List<Request> requests = Stream.concat(SpecificationService.getRequests(specificationIdentifier).stream(), Stream.of(request)).toList();");
    code.add(CompilerUtils.buildLinePadding(2) + "SpecificationService.addRequest(specificationIdentifier, request);");
    code.add(CompilerUtils.buildLinePadding(2) + "this.state = requests.get(0).getVariables();");

    for (int i = subFormulas.size() - 1; i >= 0; i--) {
      code.add(CompilerUtils.buildLinePadding(2) + String.format(
        "pre[%s] = %s;",
        i,
        subFormulas.get(i).translatePre(subFormulas)
      ));
    }
    code.add("");
    code.add(CompilerUtils.buildLinePadding(2) + "for (int i = 2; i < requests.size(); i++) {");
    code.add(CompilerUtils.buildLinePadding(3) + "this.state = requests.get(i).getVariables();");

    for (int i = subFormulas.size() - 1; i >= 0; i--) {
      code.add(CompilerUtils.buildLinePadding(3) + String.format(
        "now[%s] = %s;",
        i,
        subFormulas.get(i).translateNow(subFormulas)
      ));
    }

    code.add(CompilerUtils.buildLinePadding(2) + "}");
    code.add("");
    code.add(CompilerUtils.buildLinePadding(2) + String.format(
      "System.arraycopy(now, 0, pre, 0, %s);",
      subFormulas.size()
    ));
    code.add(CompilerUtils.buildLinePadding(2) + "return now[0];");
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
