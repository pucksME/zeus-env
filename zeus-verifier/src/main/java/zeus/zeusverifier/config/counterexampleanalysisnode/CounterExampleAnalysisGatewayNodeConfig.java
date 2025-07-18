package zeus.zeusverifier.config.counterexampleanalysisnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.GatewayNode;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;

public class CounterExampleAnalysisGatewayNodeConfig extends GatewayNodeConfig {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public CounterExampleAnalysisGatewayNodeConfig(String type, String port, GatewayNode gatewayNode) {
    super(type, port);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
