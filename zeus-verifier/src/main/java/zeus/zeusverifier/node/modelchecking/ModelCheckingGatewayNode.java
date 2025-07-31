package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.modelchecking.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingGatewayNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

public class ModelCheckingGatewayNode extends GatewayNode<ModelCheckingGatewayNodeConfig> {
  public ModelCheckingGatewayNode(ModelCheckingGatewayNodeConfig config) {
    super(config, NodeType.MODEL_CHECKING);
  }

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running processClientCodeModuleRoute");

    return new RouteResult(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
    ));
  }

  private RouteResult processStartModelCheckingRequestRoute(Message<StartModelCheckingRequest> message, Socket requestSocket) {
    System.out.println("Running processStartModelCheckingRequestRoute route");

    return new RouteResult(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ANY)
    ));
  }

  private RouteResult processDistributeModelCheckingRequestRoute(
    Message<DistributeModelCheckingRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running processDistributeModelCheckingRequestRoute route");

    for (StartModelCheckingRequest startModelCheckingRequest: message.getPayload().getStartModelCheckingRequests()) {
      this.sendMessageToNode(new Message<>(startModelCheckingRequest));
    }

    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNode.class, this::registerNodeRoute,
        DistributeModelCheckingRequest.class, this::processDistributeModelCheckingRequestRoute
      )
    );
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::processClientCodeModuleRoute,
        StartModelCheckingRequest.class, this::processStartModelCheckingRequestRoute,
        DistributeModelCheckingRequest.class, this::processDistributeModelCheckingRequestRoute
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
