package zeus.zeusverifier.config;

public abstract class Config {
  String type;

  public Config(String type) {
    this.type = type;
  }

  public NodeType getNodeType() {
    return switch (this.type) {
      case "root-node" -> NodeType.ROOT_NODE;
      case "model-checking-node" -> NodeType.MODEL_CHECKING_NODE;
      case "model-checking-gateway-node" -> NodeType.MODEL_CHECKING_GATEWAY_NODE;
      case "abstraction-gateway-node" -> NodeType.ABSTRACTION_GATEWAY_NODE;
      case "abstraction-node" -> NodeType.ABSTRACTION_NODE;
      case "counterexample-analysis-node" -> NodeType.COUNTER_EXAMPLE_NODE;
      case "counterexample-analysis-gateway-node" -> NodeType.COUNTER_EXAMPLE_GATEWAY_NODE;
      case "storage-node" -> NodeType.STORAGE_NODE;
      case "storage-gateway-node" -> NodeType.STORAGE_GATEWAY_NODE;
      default -> throw new RuntimeException(String.format(
        "Could not get node type: unknown node type \"%s\"",
        this.type
      ));
    };
  }

  public String getType() {
    return type;
  }
}
