package zeus.zeuscompiler.symboltable;

public class ClientSymbolTableIdentifier extends SymbolTableIdentifier {
  @Override
  public int hashCode() {
    return "rootClient".hashCode();
  }
}
