package zeus.zeusverifier.config.modelcheckingnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.Config;

public class ModelCheckingNodeConfig extends Config {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public ModelCheckingNodeConfig(String type, GatewayNode gatewayNode) {
    super(type);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
