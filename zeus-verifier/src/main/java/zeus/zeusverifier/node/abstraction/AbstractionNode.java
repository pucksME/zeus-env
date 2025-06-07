package zeus.zeusverifier.node.abstraction;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.shared.message.payload.abstraction.AbstractResponse;
import zeus.shared.message.payload.abstraction.AbstractionLiteral;
import zeus.zeusverifier.config.abstractionnode.AbstractionNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class AbstractionNode extends Node<AbstractionNodeConfig> {
  public AbstractionNode(AbstractionNodeConfig config) {
    super(config);
  }

  private RouteResult processAbstractRequestRoute(Message<AbstractRequest> message, Socket requestSocket) {
    System.out.printf("Running processAbstractRequestRoute for uuid \"%s\"%n", message.getPayload().uuid());
    this.sendMessage(new Message<>(
      new AbstractResponse(message.getPayload().uuid(), AbstractionLiteral.TRUE),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
    ));
    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
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
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
