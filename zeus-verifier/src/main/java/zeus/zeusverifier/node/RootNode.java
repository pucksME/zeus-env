package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.abstraction.AbstractionFailedMissingPredicateValuation;
import zeus.shared.message.payload.modelchecking.CalibrationFailed;
import zeus.shared.message.payload.modelchecking.ExpressionVariableInformationNotPresent;
import zeus.shared.message.payload.modelchecking.UnsupportedComponent;
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

  private RouteResult processCalibrationFailedRoute(Message<CalibrationFailed> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not calibrate path \"%s\"%n",
      message.getPayload().uuid(),
      message.getPayload().path()
    );
    this.terminate();
    return new RouteResult();
  }

  private RouteResult processAbstractionFailedMissingPredicateValuationRoute(
    Message<AbstractionFailedMissingPredicateValuation> message,
    Socket requestSocket
  ) {
    System.out.printf(
      "Abstraction node \"%s\" could not perform abstraction: missing valuation for predicate \"%s\"%n",
      message.getPayload().nodeUuid(),
      message.getPayload().predicateUuid()
    );
    this.terminate();
    return new RouteResult();
  }

  private RouteResult processUnsupportedComponentRoute(Message<UnsupportedComponent> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not perform model checking: unsupported component type \"%s\"%n",
      message.getPayload().nodeUuid(),
      message.getPayload().componentName()
    );
    this.terminate();
    return new RouteResult();
  }

  private RouteResult processExpressionVariableInformationNotPresentRoute(Message<ExpressionVariableInformationNotPresent> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not perform model checking: variable information for expression at %s:%s not present%n",
      message.getPayload().nodeUuid(),
      message.getPayload().line(),
      message.getPayload().linePosition()
    );
    this.terminate();
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
        VerificationResponse.class, this::processVerificationResponseRoute,
        CalibrationFailed.class, this::processCalibrationFailedRoute,
        AbstractionFailedMissingPredicateValuation.class, this::processAbstractionFailedMissingPredicateValuationRoute,
        UnsupportedComponent.class, this::processUnsupportedComponentRoute,
        ExpressionVariableInformationNotPresent.class, this::processExpressionVariableInformationNotPresentRoute
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
