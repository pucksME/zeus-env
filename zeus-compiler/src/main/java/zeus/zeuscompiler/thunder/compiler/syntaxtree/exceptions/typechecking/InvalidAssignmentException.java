package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

import zeus.zeuscompiler.thunder.compiler.utils.ThunderUtils;

public class InvalidAssignmentException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Invalid assignment";
  }
}
