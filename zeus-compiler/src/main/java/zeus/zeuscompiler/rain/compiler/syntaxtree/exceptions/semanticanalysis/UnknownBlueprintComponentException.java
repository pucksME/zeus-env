package zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis;

public class UnknownBlueprintComponentException extends SemanticAnalysisException {
  String id;

  public UnknownBlueprintComponentException(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return String.format("Unknown blueprint component \"%s\"", this.id);
  }
}
