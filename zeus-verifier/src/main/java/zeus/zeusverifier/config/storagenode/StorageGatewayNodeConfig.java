package zeus.zeusverifier.config.storagenode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.GatewayNode;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;

public class StorageGatewayNodeConfig extends GatewayNodeConfig {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public StorageGatewayNodeConfig(String type, String port, GatewayNode gatewayNode) {
    super(type, port);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
