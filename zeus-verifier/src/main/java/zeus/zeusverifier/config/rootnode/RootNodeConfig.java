package zeus.zeusverifier.config.rootnode;

import zeus.zeusverifier.config.Config;

public class RootNodeConfig extends Config {
  String host;
  String port;

  public RootNodeConfig(String type, String host, String port) {
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
