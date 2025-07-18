package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.Message;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleRequest;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisGatewayNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CounterexampleAnalysisGatewayNode extends GatewayNode<CounterExampleAnalysisGatewayNodeConfig> {
  public CounterexampleAnalysisGatewayNode(CounterExampleAnalysisGatewayNodeConfig config) {
    super(config, NodeType.COUNTEREXAMPLE_ANALYSIS);
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

  private RouteResult processAnalyzeCounterexampleRequestRoute(
    Message<AnalyzeCounterExampleRequest> message,
    Socket requestSocket
  ) {
    Optional<UUID> uuidOptional = this.sendMessageToNode(message);
    if (uuidOptional.isEmpty()) {
      System.out.println("Could not send analyze counterexample request: sending message to an counterexample analysis node failed");
      return new RouteResult(NodeAction.TERMINATE);
    }
    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AnalyzeCounterExampleRequest.class, this::processAnalyzeCounterexampleRequestRoute
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
