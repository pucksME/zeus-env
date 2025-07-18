package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleRequest;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleResponse;
import zeus.shared.message.payload.modelchecking.Path;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class CounterexampleAnalysisNode extends Node<CounterExampleAnalysisNodeConfig> {
  ClientCodeModule clientCodeModule;

  public CounterexampleAnalysisNode(CounterExampleAnalysisNodeConfig config) {
    super(config);
    this.clientCodeModule = null;
  }

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running processClientCodeModuleRoute");
    this.clientCodeModule = message.getPayload();
    return new RouteResult();
  }

  private RouteResult processAnalyzeCounterexampleRequestRoute(
    Message<AnalyzeCounterExampleRequest> message,
    Socket requestSocket
  ) {
    System.out.printf("Running processAnalyzeCounterexampleRequestRoute for uuid \"%s\"%n", message.getPayload().uuid());

    return new RouteResult(new Message<>(
      new AnalyzeCounterExampleResponse(message.getPayload().uuid(), new Path(new ArrayList<>())),
      new Recipient(NodeType.ROOT)
    ));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::processClientCodeModuleRoute,
        AbstractRequest.class, this::processAnalyzeCounterexampleRequestRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
