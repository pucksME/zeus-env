package zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis;

public class AmbiguousElementException extends SemanticAnalysisException {
  String name;
  AmbiguousElementType ambiguousElementType;

  public AmbiguousElementException(String name, AmbiguousElementType ambiguousElementType) {
    this.name = name;
    this.ambiguousElementType = ambiguousElementType;
  }

  @Override
  public String toString() {
    return String.format(
      "Ambiguous %s \"%s\"",
      switch (this.ambiguousElementType) {
        case VIEW -> "view";
        case BLUEPRINT_COMPONENT -> "blueprint component";
        case COMPONENT -> "component";
        case SHAPE -> "shape";
      },
      this.name
    );
  }
}
