package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

public class InvalidAssignmentException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Invalid assignment";
  }
}
