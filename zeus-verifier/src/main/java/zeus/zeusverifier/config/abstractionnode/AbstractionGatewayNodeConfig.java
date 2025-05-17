package zeus.zeusverifier.config.abstractionnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.GatewayNode;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;

public class AbstractionGatewayNodeConfig extends GatewayNodeConfig {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public AbstractionGatewayNodeConfig(String type, String port, GatewayNode gatewayNode) {
    super(type, port);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
