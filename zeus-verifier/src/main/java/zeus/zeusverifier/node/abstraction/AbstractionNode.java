package zeus.zeusverifier.node.abstraction;

import zeus.shared.message.Message;
import zeus.zeusverifier.config.abstractionnode.AbstractionNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class AbstractionNode extends Node<AbstractionNodeConfig> {
  public AbstractionNode(AbstractionNodeConfig config) {
    super(config);
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of()
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
