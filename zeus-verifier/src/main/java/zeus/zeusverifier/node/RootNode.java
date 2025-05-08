package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.modelchecking.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class RootNode extends GatewayNode<RootNodeConfig> {
  Socket modelCheckingGatewayNodeSocket;
  ExecutorService gatewayNodesExecutorService;

  public RootNode(RootNodeConfig config) {
    super(config);
    this.modelCheckingGatewayNodeSocket = null;
    this.gatewayNodesExecutorService = Executors.newCachedThreadPool();
  }

  private RouteResult registerGatewayNodeRoute(Message<RegisterGatewayNode> message, Socket requestSocket) {
    System.out.println("Running registerGatewayNodeRoute");

    switch (message.getPayload().type()) {
      case MODEL_CHECKING -> {
        this.modelCheckingGatewayNodeSocket = requestSocket;
      }
    };

    return new RouteResult(new Message<>(new RegisterNodeResponse(UUID.randomUUID())));
  }

  private RouteResult verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    throw new UnsupportedOperationException("Not implemented yet");
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterGatewayNode.class, this::registerGatewayNodeRoute,
        ClientCodeModule.class, this::verifyRoute
//        RegisterModelCheckingNodeRequest.class, this::registerModelCheckingNodeRoute,
//        SetCodeModuleResponse.class, this::processSetCodeModuleResponseRoute,
//        StartModelCheckingResponse.class, this::processStartModelCheckingResponseRoute,
//        CalibrationFailed.class, this::processCalibrationFailedRoute
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
