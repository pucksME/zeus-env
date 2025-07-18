package zeus.zeusverifier.config.counterexampleanalysisnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.Config;
import zeus.zeusverifier.config.GatewayNode;

public class CounterExampleAnalysisNodeConfig extends Config {
  @SerializedName("gateway-node")
  GatewayNode gatewayNode;

  public CounterExampleAnalysisNodeConfig(String type, GatewayNode gatewayNode) {
    super(type);
  }

  public GatewayNode getRootNode() {
    return gatewayNode;
  }
}
