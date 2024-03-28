package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

public class IncompatibleTypeException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Incompatible type";
  }
}
