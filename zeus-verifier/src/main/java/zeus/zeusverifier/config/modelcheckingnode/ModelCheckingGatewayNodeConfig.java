package zeus.zeusverifier.config.modelcheckingnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;

public class ModelCheckingGatewayNodeConfig extends GatewayNodeConfig {
  @SerializedName("root-node")
  GatewayNode gatewayNode;

  public ModelCheckingGatewayNodeConfig(String type, String host, String port, GatewayNode gatewayNode) {
    super(type, host, port);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
