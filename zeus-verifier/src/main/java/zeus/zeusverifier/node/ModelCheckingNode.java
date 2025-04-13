package zeus.zeusverifier.node;

import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingNodeConfig;

import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class ModelCheckingNode extends Node {
  ModelCheckingNodeConfig config;
  public ModelCheckingNode(ModelCheckingNodeConfig config) {
    super(config);
  }

  @Override
  public void start() throws IOException {
    String rootNodePort = this.config.getRootNode().getPort();
    Optional<Integer> rootNodePortOptional = Main.parsePort(rootNodePort);

    if (rootNodePortOptional.isEmpty()) {
      throw new RuntimeException(String.format(
        "Could not register model checking node: invalid root node port \"%s\"",
        rootNodePort
      ));
    }

    try (Socket socket = new Socket(this.config.getRootNode().getHost(), rootNodePortOptional.get())) {
    }

    super.start();
  }

  @Override
  public void run(Socket requestSocket) {
    System.out.println("running model checking node procedure");
  }
}
