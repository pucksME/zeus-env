package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.VerificationResponse;
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

  private RouteResult verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running verify route");

    if (this.getNodes().isEmpty()) {
      System.out.println("Could not verify code module: no model checking nodes available");
      return new RouteResult(new Message<>(new VerificationResponse(false)));
    }

    this.sendBroadcastMessage(new Message<>(message.getPayload()));

    UUID nodeUuid = this.getNodes().keys().nextElement();
    this.sendMessage(
      new Message<>(new StartModelCheckingRequest(new Path(new ArrayList<>()), new HashSet<>(), new HashSet<>())),
      this.getNodes().get(nodeUuid)
    );

    return new RouteResult(new Message<>(new VerificationResponse(false)));
  }

  private RouteResult processSetCodeModuleResponseRoute(Message<SetCodeModuleResponse> message, Socket requestSocket) {
    System.out.println("Running processSetCodeModuleResponse route");
    return new RouteResult();
  }

  private RouteResult processStartModelCheckingResponseRoute(
    Message<StartModelCheckingResponse> message,
    Socket requestSocket
  ) {
    System.out.println("Running processStartModelCheckingResponseRoute route");
    return new RouteResult();
  }

  private RouteResult processDistributeModelCheckingRequestRoute(
    Message<DistributeModelCheckingRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running processDistributeModelCheckingRequestRoute route");
    for (Map<UUID, PredicateValuation> predicateValuations : message.getPayload().predicateValuations()) {
      this.sendMessageToNode(new Message<>(new StartModelCheckingRequest(
        message.getPayload().path(),
        new HashSet<>(message.getPayload().predicates().values()),
        new HashSet<>(predicateValuations.values())
      )));
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
        SetCodeModuleResponse.class, this::processSetCodeModuleResponseRoute,
        StartModelCheckingResponse.class, this::processStartModelCheckingResponseRoute,
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
        ClientCodeModule.class, this::verifyRoute
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
