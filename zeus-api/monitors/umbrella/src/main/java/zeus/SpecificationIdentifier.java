package zeus;

public class SpecificationIdentifier {
  String context;
  String serverName;
  String routeId;

  public SpecificationIdentifier(String context, String serverName, String routeId) {
    this.context = context;
    this.serverName = serverName;
    this.routeId = routeId;
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
