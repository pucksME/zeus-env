package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class RootNode extends GatewayNode<GatewayNodeConfig> {
  Socket modelCheckingGatewayNodeSocket;
  Socket abstractionGatewayNodeSocket;
  ExecutorService gatewayNodesExecutorService;

  public RootNode(GatewayNodeConfig config) {
    super(config);
    this.modelCheckingGatewayNodeSocket = null;
    this.abstractionGatewayNodeSocket = null;
    this.gatewayNodesExecutorService = Executors.newCachedThreadPool();
  }

  private RouteResult registerGatewayNodeRoute(Message<RegisterNode> message, Socket requestSocket) {
    System.out.println("Running registerGatewayNodeRoute");

    switch (message.getPayload().type()) {
      case MODEL_CHECKING_GATEWAY -> {
        this.modelCheckingGatewayNodeSocket = requestSocket;
      }
      case ABSTRACTION_GATEWAY -> {
        this.abstractionGatewayNodeSocket = requestSocket;
      }
    };

    try {
      this.registerNode(requestSocket, this.nodes, this.nodesExecutorService);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new RouteResult(new Message<>(new RegisterNodeResponse(UUID.randomUUID())));
  }

  private RouteResult verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    this.sendMessage(message, this.modelCheckingGatewayNodeSocket);
    return new RouteResult(new Message<>(new VerificationResponse(false)));
  }

  private RouteResult processVerificationResponseRoute(Message<VerificationResponse> message, Socket requestSocket) {
    System.out.println("Running processVerificationResponseRoute");
    return new RouteResult();
  }

  private RouteResult processAbstractRequestRoute(Message<AbstractRequest> message, Socket requestSocket) {
    System.out.println("Running processAbstractRequestRoute");
    this.sendMessage(message, this.abstractionGatewayNodeSocket);
    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNode.class, this::registerGatewayNodeRoute,
        ClientCodeModule.class, this::verifyRoute,
        AbstractRequest.class, this::processAbstractRequestRoute,
        VerificationResponse.class, this::processVerificationResponseRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayServer();
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    throw new UnsupportedOperationException("Unsupported operation");
  }
}
