package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port;

public class CodeModuleResponseExpression extends CodeModuleInputExpression {
  public CodeModuleResponseExpression(int line, int linePosition, String inputId) {
    super(line, linePosition, "response", inputId);
  }
}
