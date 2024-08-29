package zeus.zeuscompiler.symboltable;

public class ServerRouteSymbolTableIdentifier extends SymbolTableIdentifier {
  String serverName;
  String routeId;

  public ServerRouteSymbolTableIdentifier(String serverName, String routeId) {
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
    if (!(obj instanceof ServerRouteSymbolTableIdentifier)) {
      return false;
    }

    return this.getId().equals(((ServerRouteSymbolTableIdentifier) obj).getId());
  }
}
