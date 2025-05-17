package zeus.zeusverifier.config.abstractionnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.Config;
import zeus.zeusverifier.config.GatewayNode;

public class AbstractionNodeConfig extends Config {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public AbstractionNodeConfig(String type, GatewayNode gatewayNode) {
    super(type);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
