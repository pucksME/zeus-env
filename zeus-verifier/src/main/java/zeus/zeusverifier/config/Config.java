package zeus.zeusverifier.config;

public abstract class Config {
  String type;
  String host;
  String port;

  public Config(String type, String host, String port) {
    this.type = type;
    this.host = host;
    this.port = port;
  }

  public NodeType getNodeType() {
    return switch (this.type) {
      case "root-node" -> NodeType.ROOT_NODE;
      case "model-checking-node" -> NodeType.MODEL_CHECKING_NODE;
      default -> throw new RuntimeException(String.format(
        "Could not get node type: unknown node type \"%s\"",
        this.type
      ));
    };
  }

  public String getType() {
    return type;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }
}
