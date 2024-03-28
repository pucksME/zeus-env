package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

public class UnknownCodeModuleException extends TypeCheckingException {
  @Override
  public String toString() {
    return "Unknown code module";
  }
}
