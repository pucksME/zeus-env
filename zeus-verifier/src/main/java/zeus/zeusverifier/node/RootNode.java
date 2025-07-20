package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.abstraction.AbstractionFailed;
import zeus.shared.message.payload.counterexampleanalysis.CounterexampleAnalysisFailed;
import zeus.shared.message.payload.modelchecking.*;
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
  Socket counterexampleAnalysisGatewayNodeSocket;
  ExecutorService gatewayNodesExecutorService;

  public RootNode(GatewayNodeConfig config) {
    super(config);
    this.modelCheckingGatewayNodeSocket = null;
    this.abstractionGatewayNodeSocket = null;
    this.counterexampleAnalysisGatewayNodeSocket = null;
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
      case COUNTEREXAMPLE_ANALYSIS_GATEWAY -> {
        this.counterexampleAnalysisGatewayNodeSocket = requestSocket;
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
    this.sendMessage(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.COUNTEREXAMPLE_ANALYSIS, NodeSelection.ALL)
    ), this.counterexampleAnalysisGatewayNodeSocket);
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

  private RouteResult processUnsupportedComponentRoute(Message<UnsupportedComponent> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not perform model checking: unsupported component type \"%s\"%n",
      message.getPayload().nodeUuid(),
      message.getPayload().componentName()
    );
    this.terminate();
    return new RouteResult();
  }

  private RouteResult processModelCheckingFailedRoute(Message<ModelCheckingFailed> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not perform model checking: (%s)%n",
      message.getPayload().nodeUuid(),
      message.getPayload().message()
    );
    this.terminate();
    return new RouteResult();
  }

  private RouteResult processNoCounterexampleFoundRoute(Message<NoCounterexampleFound> message, Socket requestSocket) {
    System.out.printf("Model checking node \"%s\" could not find a counterexample%n", message.getPayload().nodeUuid());
    return new RouteResult();
  }

  private RouteResult processAbstractionFailedRoute(Message<AbstractionFailed> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not perform model checking: abstraction failed (%s)%n",
      message.getPayload().nodeUuid(),
      message.getPayload().message()
    );
    this.terminate();
    return new RouteResult();
  }

  private RouteResult processCounterexampleAnalysisFailedRoute(
    Message<CounterexampleAnalysisFailed> message,
    Socket requestSocket
  ) {
    System.out.printf(
      "Counterexample analysis node \"%s\" could not perform counterexample analysis: counterexample analysis failed (%s)%n",
      message.getPayload().nodeUuid(),
      message.getPayload().message()
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
        UnsupportedComponent.class, this::processUnsupportedComponentRoute,
        ModelCheckingFailed.class, this::processModelCheckingFailedRoute,
        NoCounterexampleFound.class, this::processNoCounterexampleFoundRoute,
        AbstractionFailed.class, this::processAbstractionFailedRoute,
        CounterexampleAnalysisFailed.class, this::processCounterexampleAnalysisFailedRoute
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
