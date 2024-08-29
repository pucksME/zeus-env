package zeus.zeuscompiler.symboltable;

public class ClientSymbolTableIdentifier extends SymbolTableIdentifier {
  private final String id;

  public ClientSymbolTableIdentifier() {
    this.id = "rootClient";
  }

  @Override
  public int hashCode() {
    return this.id.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ClientSymbolTableIdentifier)) {
      return false;
    }

    return this.id.equals(((ClientSymbolTableIdentifier) obj).id);
  }
}
