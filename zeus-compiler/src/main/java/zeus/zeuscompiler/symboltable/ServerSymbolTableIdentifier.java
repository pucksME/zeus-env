package zeus.zeuscompiler.symboltable;

public class ServerSymbolTableIdentifier extends SymbolTableIdentifier {
  String serverName;
  String routeId;

  public ServerSymbolTableIdentifier(String serverName, String routeId) {
    this.serverName = serverName;
    this.routeId = routeId;
  }

  private String getId() {
    return String.format("%s/%s", this.serverName, this.routeId);
  }
  @Override
  public int hashCode() {
    return this.getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof ServerSymbolTableIdentifier)) {
      return false;
    }

    return this.getId().equals(((ServerSymbolTableIdentifier) obj).getId());
  }
}
