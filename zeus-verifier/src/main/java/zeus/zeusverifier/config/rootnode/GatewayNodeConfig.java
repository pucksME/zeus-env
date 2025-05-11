package zeus.zeusverifier.config.rootnode;

import zeus.zeusverifier.config.Config;

public class GatewayNodeConfig extends Config {
  String host;
  String port;

  public GatewayNodeConfig(String type, String host, String port) {
    super(type);
    this.host = host;
    this.port = port;
  }

  public String getHost() {
    return host;
  }

  public String getPort() {
    return port;
  }
}
