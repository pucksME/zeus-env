package zeus.zeuscompiler.symboltable;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

public class VariableInformation {
  Type type;
  VariableType variableType;
  int declarationLine;
  int declarationLinePosition;

  public VariableInformation(Type type, VariableType variableType, int declarationLine, int declarationLinePosition) {
    this.type = type;
    this.variableType = variableType;
    this.declarationLine = declarationLine;
    this.declarationLinePosition = declarationLinePosition;
  }

  public Type getType() {
    return type;
  }

  public VariableType getVariableType() {
    return variableType;
  }
}
