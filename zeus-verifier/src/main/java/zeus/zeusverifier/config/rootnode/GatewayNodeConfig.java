package zeus.zeusverifier.config.rootnode;

import zeus.zeusverifier.config.Config;

public class GatewayNodeConfig extends Config {
  String port;

  public GatewayNodeConfig(String type, String port) {
    super(type);
    this.port = port;
  }

  public String getPort() {
    return port;
  }
}
