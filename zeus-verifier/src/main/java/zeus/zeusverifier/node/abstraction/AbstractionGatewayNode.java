package zeus.zeusverifier.node.abstraction;

import zeus.shared.message.Message;
import zeus.shared.message.payload.RegisterNode;
import zeus.zeusverifier.config.abstractionnode.AbstractionGatewayNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class AbstractionGatewayNode extends GatewayNode<AbstractionGatewayNodeConfig> {
  public AbstractionGatewayNode(AbstractionGatewayNodeConfig config) {
    super(config);
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNode.class, this::registerNodeRoute
      )
    );
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
    new Thread(() -> {
      try {
        this.startGatewayServer();
      } catch (IOException ioException) {
        throw new RuntimeException(ioException);
      }
    }).start();

    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
