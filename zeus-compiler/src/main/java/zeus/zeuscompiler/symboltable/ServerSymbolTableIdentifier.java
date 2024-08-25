package zeus.zeuscompiler.symboltable;

public class ServerSymbolTableIdentifier extends SymbolTableIdentifier {
  String serverName;
  String routeId;

  public ServerSymbolTableIdentifier(String serverName, String routeId) {
    this.serverName = serverName;
    this.routeId = routeId;
  }

  @Override
  public int hashCode() {
    return String.format("%s/%s", this.serverName, this.routeId).hashCode();
  }
}
