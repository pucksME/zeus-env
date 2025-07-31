package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleRequest;
import zeus.shared.message.payload.counterexampleanalysis.InvalidCounterexample;
import zeus.shared.message.payload.counterexampleanalysis.ValidCounterexample;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

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

    CounterexampleAnalyzer counterexampleAnalyzer = new CounterexampleAnalyzer(
      message.getPayload().path(),
      this.clientCodeModule,
      this
    );

    Optional<Counterexample> counterexampleOptional = counterexampleAnalyzer.analyze();

    if (counterexampleOptional.isEmpty()) {
      return new RouteResult();
    }

    Counterexample counterexample = counterexampleOptional.get();

    if (counterexample.valid()) {
      return new RouteResult(new Message<>(
        new ValidCounterexample(message.getPayload().uuid(), counterexample.path()),
        new Recipient(NodeType.ROOT)
      ));
    }

    return new RouteResult(new Message<>(
      new InvalidCounterexample(message.getPayload().uuid(), counterexample.path()),
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
        AnalyzeCounterExampleRequest.class, this::processAnalyzeCounterexampleRequestRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
