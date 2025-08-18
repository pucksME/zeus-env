package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractionFailed;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleRequest;
import zeus.shared.message.payload.counterexampleanalysis.InvalidCounterexample;
import zeus.shared.message.payload.counterexampleanalysis.ValidCounterexample;
import zeus.shared.message.payload.modelchecking.StopModelCheckingTaskRequest;
import zeus.shared.message.payload.modelchecking.StopModelCheckingTaskRequestStatus;
import zeus.shared.message.payload.modelchecking.SynchronizedCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CounterexampleAnalysisNode extends Node<CounterExampleAnalysisNodeConfig> {
  ConcurrentHashMap<UUID, ClientCodeModule> codeModules;

  public CounterexampleAnalysisNode(CounterExampleAnalysisNodeConfig config) {
    super(config);
    this.codeModules = new ConcurrentHashMap<>();
  }

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running processClientCodeModuleRoute");
    Optional<UUID> verificationUuidOptional = message.getPayload().getVerificationUuid();

    if (verificationUuidOptional.isEmpty()) {
      return new RouteResult(new Message<>(
        new AbstractionFailed(this.getUuid(), "missing verification uuid in client code module"),
        new Recipient(NodeType.ROOT)
      ), NodeAction.TERMINATE);
    }

    UUID verificationUuid = verificationUuidOptional.get();
    this.codeModules.put(verificationUuid, message.getPayload());
    return new RouteResult(new Message<>(
      new SynchronizedCodeModule(this.getUuid(), verificationUuid),
      new Recipient(NodeType.ROOT)
    ));
  }

  private RouteResult processAnalyzeCounterexampleRequestRoute(
    Message<AnalyzeCounterExampleRequest> message,
    Socket requestSocket
  ) {
    System.out.printf(
      "Running processAnalyzeCounterexampleRequestRoute for verification uuid \"%s\"%n",
      message.getPayload().verificationUuid()
    );
    ClientCodeModule codeModule = codeModules.get(message.getPayload().verificationUuid());

    if (codeModule == null) {
      return new RouteResult(new Message<>(
        new AbstractionFailed(
          this.getUuid(),
          String.format("missing code module for verification uuid \"%s\"", message.getPayload().verificationUuid())
        ),
        new Recipient(NodeType.ROOT)
      ), NodeAction.TERMINATE);
    }

    CounterexampleAnalyzer counterexampleAnalyzer = new CounterexampleAnalyzer(
      message.getPayload().path(),
      codeModule,
      this
    );

    Optional<Counterexample> counterexampleOptional = counterexampleAnalyzer.analyze();

    if (counterexampleOptional.isEmpty()) {
      System.out.println("Counterexample analysis, no new predicates");
      return new RouteResult(new Message<>(
        new StopModelCheckingTaskRequest(
          message.getPayload().verificationUuid(),
          message.getPayload().modelCheckingTaskUuid(),
          StopModelCheckingTaskRequestStatus.NO_NEW_PREDICATES
        ),
        new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
      ));
    }

    Counterexample counterexample = counterexampleOptional.get();

    if (counterexample.valid()) {
      return new RouteResult(new Message<>(
        new ValidCounterexample(
          message.getPayload().verificationUuid(),
          message.getPayload().modelCheckingTaskUuid(),
          counterexample.path()
        ),
        new Recipient(NodeType.ROOT)
      ));
    }

    return new RouteResult(new Message<>(
      new InvalidCounterexample(
        message.getPayload().verificationUuid(),
        message.getPayload().modelCheckingTaskUuid(),
        counterexample.path()
      ),
      new Recipient(NodeType.ROOT)
    ));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
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
