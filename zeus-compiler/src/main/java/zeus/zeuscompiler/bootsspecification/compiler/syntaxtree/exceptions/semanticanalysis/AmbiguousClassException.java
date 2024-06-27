package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.exceptions.semanticanalysis;

import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.SemanticAnalysisException;

public class AmbiguousClassException extends SemanticAnalysisException {
  String id;

  public AmbiguousClassException(String id) {
    this.id = id;
  }

  @Override
  public String toString() {
    return String.format("Ambiguous boots specification class \"%s\"", this.id);
  }
}
