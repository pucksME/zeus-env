package zeus.zeusverifier.config.storagenode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.Config;
import zeus.zeusverifier.config.GatewayNode;

public class StorageNodeConfig extends Config {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public StorageNodeConfig(String type, GatewayNode gatewayNode) {
    super(type);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
