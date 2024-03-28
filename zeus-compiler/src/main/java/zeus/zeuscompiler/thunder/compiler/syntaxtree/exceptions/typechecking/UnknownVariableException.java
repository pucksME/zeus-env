package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

public class UnknownVariableException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Unknown variable";
  }
}
