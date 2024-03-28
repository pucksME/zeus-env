package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

public class UnknownObjectPropertyException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Unknown object property";
  }
}
