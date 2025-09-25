package zeus.zeusverifier.node.abstraction;

import zeus.shared.message.Message;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNodeRequest;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.zeusverifier.config.abstractionnode.AbstractionGatewayNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AbstractionGatewayNode extends GatewayNode<AbstractionGatewayNodeConfig> {
  public AbstractionGatewayNode(AbstractionGatewayNodeConfig config) {
    super(config, NodeType.ABSTRACTION);
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNodeRequest.class, this::registerNodeRoute
      )
    );
  }

  private RouteResult processAbstractRequestRoute(Message<AbstractRequest> message, Socket requestSocket) {
    Optional<UUID> uuidOptional = this.sendMessageToNode(message);
    if (uuidOptional.isEmpty()) {
      System.out.println("Could not send abstract request: sending message to an abstraction node failed");
      return new RouteResult(NodeAction.TERMINATE);
    }
    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AbstractRequest.class, this::processAbstractRequestRoute
      )
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
