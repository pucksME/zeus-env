package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port;

public class CodeModuleRequestExpression extends CodeModuleOutputExpression {
    RequestParameterType requestParameterType;
    public CodeModuleRequestExpression(
      int line,
      int linePosition,
      String outputId,
      RequestParameterType requestParameterType
    ) {
        super(line, linePosition, "request", outputId);
        this.requestParameterType = requestParameterType;
    }

    public RequestParameterType getRequestParameterType() {
        return requestParameterType;
    }
}
