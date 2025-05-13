package zeus.zeusverifier.config.modelcheckingnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;

public class ModelCheckingGatewayNodeConfig extends GatewayNodeConfig {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public ModelCheckingGatewayNodeConfig(String type, String port, GatewayNode gatewayNode) {
    super(type, port);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
