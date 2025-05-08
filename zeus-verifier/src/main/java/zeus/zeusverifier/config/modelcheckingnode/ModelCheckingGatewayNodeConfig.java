package zeus.zeusverifier.config.modelcheckingnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;

public class ModelCheckingGatewayNodeConfig extends RootNodeConfig {
  @SerializedName("root-node")
  RootNode rootNode;

  public ModelCheckingGatewayNodeConfig(String type, String host, String port, RootNode rootNode) {
    super(type, host, port);
  }

  public RootNode getRootNode() {
    return rootNode;
  }
}
