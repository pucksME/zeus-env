package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.abstraction.AbstractionFailed;
import zeus.shared.message.payload.counterexampleanalysis.InvalidCounterexample;
import zeus.shared.message.payload.counterexampleanalysis.ValidCounterexample;
import zeus.shared.message.payload.counterexampleanalysis.CounterexampleAnalysisFailed;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.predicate.Predicate;
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
  ConcurrentHashMap<UUID, List<CompletableFuture<UUID>>> pendingCodeModuleSynchronizations;
  ConcurrentHashMap<UUID, CompletableFuture<Path>> pendingVerificationRequests;

  public RootNode(GatewayNodeConfig config) {
    super(config);
    this.modelCheckingGatewayNodeSocket = null;
    this.abstractionGatewayNodeSocket = null;
    this.counterexampleAnalysisGatewayNodeSocket = null;
    this.gatewayNodesExecutorService = Executors.newCachedThreadPool();
    this.pendingCodeModuleSynchronizations = new ConcurrentHashMap<>();
    this.pendingVerificationRequests = new ConcurrentHashMap<>();
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

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    CompletableFuture<Path> pendingVerificationCompletableFuture = new CompletableFuture<>();
    UUID verificationUuid = UUID.randomUUID();
    this.pendingVerificationRequests.put(verificationUuid, pendingVerificationCompletableFuture);
    message.getPayload().setVerificationUuid(verificationUuid);
    this.pendingCodeModuleSynchronizations.put(
      verificationUuid,
      List.of(new CompletableFuture<>(), new CompletableFuture<>())
    );

    this.sendMessage(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
    ), this.modelCheckingGatewayNodeSocket);

    this.sendMessage(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.COUNTEREXAMPLE_ANALYSIS, NodeSelection.ALL)
    ), this.counterexampleAnalysisGatewayNodeSocket);

    List<CompletableFuture<UUID>> pendingCodeModuleSynchronization = this.pendingCodeModuleSynchronizations.get(
      verificationUuid
    );

    if (pendingCodeModuleSynchronization == null) {
      System.out.printf("Code module synchronization for verification uuid \"%s\" does not exist%n", verificationUuid);
      return new RouteResult(NodeAction.TERMINATE);
    }

    for (CompletableFuture<UUID> pendingSynchronizationCompletableFuture : this.pendingCodeModuleSynchronizations.get(
      verificationUuid
    )) {
      try {
        UUID nodeUuid = pendingSynchronizationCompletableFuture.get();
        System.out.printf(
          "Synchronized code module for verification uuid \"%s\" and node \"%s\"",
          verificationUuid,
          nodeUuid
        );
      } catch (InterruptedException | ExecutionException e) {
        System.out.printf("Could not synchronize code module for verification uuid \"%s\"%n", verificationUuid);
        return new RouteResult(NodeAction.TERMINATE);
      }
    }

    this.pendingCodeModuleSynchronizations.remove(verificationUuid);

    this.sendMessage(new Message<>(
      new StartModelCheckingRequest(verificationUuid, new Path(new ArrayList<>())),
      new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
    ), this.modelCheckingGatewayNodeSocket);

    try {
      Path counterexample = pendingVerificationCompletableFuture.get();
      return new RouteResult(new Message<>(new VerificationResponse(true)));
    } catch (InterruptedException | ExecutionException e) {
      return new RouteResult(new Message<>(new VerificationResponse(false)));
    }
  }

  private RouteResult processSynchronizedCodeModuleRoute(
    Message<SynchronizedCodeModule> message,
    Socket requestSocket
  ) {
    List<CompletableFuture<UUID>> pendingCodeModuleSynchronization = this.pendingCodeModuleSynchronizations.get(
      message.getPayload().verificationUuid()
    );

    if (pendingCodeModuleSynchronization == null) {
      System.out.printf(
        "Pending code module synchronization for verification uuid \"%s\" received from node \"%s\" does not exist%n",
        message.getPayload().verificationUuid(),
        message.getPayload().nodeUuid()
      );
      return new RouteResult(NodeAction.TERMINATE);
    }

    for (CompletableFuture<UUID> completableFuture : pendingCodeModuleSynchronization) {
      if (!completableFuture.isDone()) {
        completableFuture.complete(message.getPayload().nodeUuid());
      }
    }

    return new RouteResult();
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

  private RouteResult processValidCounterexampleRoute(Message<ValidCounterexample> message, Socket requestSocket) {
    System.out.println("Running processValidCounterexampleRoute");
    return new RouteResult();
  }

  private RouteResult processInvalidCounterexampleRoute(Message<InvalidCounterexample> message, Socket requestSocket) {
    System.out.println("Running processInvalidCounterexampleRoute");
    return new RouteResult(new Message<>(new DistributeModelCheckingRequest(
      message.getPayload().verificationUuid(),
      message.getPayload().path(),
      PredicateValuation.getCombinations(
        message.getPayload().path().states().getLast().getPredicates().orElse(new HashSet<>()).stream()
          .map(Predicate::getUuid)
          .toList()
      )), new Recipient(NodeType.MODEL_CHECKING_GATEWAY)));
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.ofEntries(
        Map.entry(RegisterNode.class, this::registerGatewayNodeRoute),
        Map.entry(ClientCodeModule.class, this::processClientCodeModuleRoute),
        Map.entry(SynchronizedCodeModule.class, this::processSynchronizedCodeModuleRoute),
        Map.entry(VerificationResponse.class, this::processVerificationResponseRoute),
        Map.entry(CalibrationFailed.class, this::processCalibrationFailedRoute),
        Map.entry(UnsupportedComponent.class, this::processUnsupportedComponentRoute),
        Map.entry(ModelCheckingFailed.class, this::processModelCheckingFailedRoute),
        Map.entry(NoCounterexampleFound.class, this::processNoCounterexampleFoundRoute),
        Map.entry(AbstractionFailed.class, this::processAbstractionFailedRoute),
        Map.entry(CounterexampleAnalysisFailed.class, this::processCounterexampleAnalysisFailedRoute),
        Map.entry(ValidCounterexample.class, this::processValidCounterexampleRoute),
        Map.entry(InvalidCounterexample.class, this::processInvalidCounterexampleRoute)
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
