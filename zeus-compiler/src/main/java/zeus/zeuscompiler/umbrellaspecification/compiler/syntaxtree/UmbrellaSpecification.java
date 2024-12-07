package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
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

  // Monitor generation algorithm adapted from
  // Havelund, Klaus, and Grigore Roşu. "Efficient monitoring of safety properties."
  // International Journal on Software Tools for Technology Transfer 6 (2004): 158-173.
  @Override
  public String translate(int depth, ExportTarget exportTarget) {
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
    code.add("@Overwrite");
    code.add(CompilerUtils.buildLinePadding(1) + "public boolean verify(Request request) {");
    code.add(CompilerUtils.buildLinePadding(2) + "List<Request> requests = Stream.concat(SpecificationService.getRequests().stream(), Stream.of(request)).toList()");
    code.add(CompilerUtils.buildLinePadding(2) + "SpecificationService.addRequest(request)");
    code.add(CompilerUtils.buildLinePadding(2) + "this.state = requests.get(0).getVariables()");
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
