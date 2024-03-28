package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

import zeus.zeuscompiler.thunder.compiler.utils.ThunderUtils;

public class MissingDeclarationException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Missing declaration";
  }
}
