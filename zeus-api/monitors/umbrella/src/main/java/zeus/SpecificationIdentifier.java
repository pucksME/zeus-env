package zeus;

public class SpecificationIdentifier {
  String serverName;
  String routeId;
  String context;

  public SpecificationIdentifier(String serverName, String routeId, String context) {
    this.serverName = serverName;
    this.routeId = routeId;
    this.context = context;
  }

  private String getId() {
    return String.format("%s/%s/%s", this.context, this.serverName, this.routeId);
  }

  @Override
  public int hashCode() {
    return this.getId().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SpecificationIdentifier)) {
      return false;
    }

    return this.getId().equals(((SpecificationIdentifier) obj).getId());
  }
}
