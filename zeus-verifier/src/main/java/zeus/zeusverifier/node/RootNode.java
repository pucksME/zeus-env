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
  ConcurrentHashMap<UUID, List<CompletableFuture<VerificationResult>>> pendingVerification;

  public RootNode(GatewayNodeConfig config) {
    super(config);
    this.modelCheckingGatewayNodeSocket = null;
    this.abstractionGatewayNodeSocket = null;
    this.counterexampleAnalysisGatewayNodeSocket = null;
    this.gatewayNodesExecutorService = Executors.newCachedThreadPool();
    this.pendingCodeModuleSynchronizations = new ConcurrentHashMap<>();
    this.pendingVerification = new ConcurrentHashMap<>();
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
    UUID verificationUuid = UUID.randomUUID();
    this.pendingVerification.put(verificationUuid, new ArrayList<>(List.of(new CompletableFuture<>())));
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
          "Synchronized code module for verification uuid \"%s\" and node \"%s\"%n",
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

    List<VerificationResult> verificationResults = new ArrayList<>();
    List<CompletableFuture<VerificationResult>> pendingVerificationResults = this.pendingVerification.get(
      verificationUuid
    );

    for (int i = 0; i < pendingVerificationResults.size(); i++) {
      CompletableFuture<VerificationResult> verificationResultCompletableFuture = pendingVerificationResults.get(i);

      try {
        VerificationResult verificationResult = verificationResultCompletableFuture.get();

        if (verificationResult.getValidCounterexample().isEmpty()) {
          break;
        }

        verificationResults.add(verificationResult);
        pendingVerificationResults.add(new CompletableFuture<>());
      } catch (InterruptedException | ExecutionException e) {
        return new RouteResult(new Message<>(new VerificationResponse()));
      }
    }

    return new RouteResult(new Message<>(new VerificationResponse(verificationResults)));
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
        break;
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

  private RouteResult processModelCheckingFailedRoute(Message<ModelCheckingFailed> message, Socket requestSocket) {
    System.out.printf(
      "Model checking node \"%s\" could not perform model checking: (%s)%n",
      message.getPayload().nodeUuid(),
      message.getPayload().message()
    );
    this.terminate();
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
    List<CompletableFuture<VerificationResult>> verificationResults = this.pendingVerification.get(
      message.getPayload().verificationUuid()
    );

    if (verificationResults == null) {
      System.out.printf(
        "No pending verification for verification uuid \"%s\" in valid counterexample%n",
        message.getPayload().verificationUuid()
      );
      return new RouteResult(NodeAction.TERMINATE);
    }

    if (verificationResults.isEmpty()) {
      System.out.printf(
        "No verification results for pending verification for verification uuid \"%s\" in valid counterexample%n",
        message.getPayload().verificationUuid()
      );
      return new RouteResult(NodeAction.TERMINATE);
    }

    verificationResults.getLast().complete(new VerificationResult(
      message.getPayload().verificationUuid(),
      message.getPayload().path()
    ));

    return new RouteResult(new Message<>(
      new StopModelCheckingTask(message.getPayload().verificationUuid()),
      new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
    ));
  }

  private RouteResult processInvalidCounterexampleRoute(Message<InvalidCounterexample> message, Socket requestSocket) {
    System.out.println("Running processInvalidCounterexampleRoute");
    this.sendMessage(new Message<>(
      new DistributeModelCheckingRequest(
        message.getPayload().verificationUuid(),
        message.getPayload().path(),
        PredicateValuation.getCombinations(
          message.getPayload().path().states().getLast().getPredicates().orElse(new HashSet<>()).stream()
            .map(Predicate::getUuid)
            .toList()
        )),
      new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
    ), this.modelCheckingGatewayNodeSocket);

    return new RouteResult(new Message<>(
      new StopModelCheckingTask(message.getPayload().verificationUuid()),
      new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
    ));
  }

  private RouteResult processVerificationResultRoute(Message<VerificationResult> message, Socket requestSocket) {
    System.out.println("Running processVerificationResultRoute");
    List<CompletableFuture<VerificationResult>> verificationResults = this.pendingVerification.get(
      message.getPayload().getVerificationUuid()
    );

    if (verificationResults == null) {
      System.out.printf(
        "Could not process verification result: no verification results for verification uuid \"%s\"%n",
        message.getPayload().getVerificationUuid()
      );

      return new RouteResult(NodeAction.TERMINATE);
    }

    try {
      verificationResults.getLast().complete(message.getPayload());
      return new RouteResult();
    } catch (NoSuchElementException noSuchElementException) {
      System.out.printf(
        "Could not process verification result: empty verification results for verification uuid \"%s\"%n",
        message.getPayload().getVerificationUuid()
      );

      return new RouteResult(NodeAction.TERMINATE);
    }
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
        Map.entry(ModelCheckingFailed.class, this::processModelCheckingFailedRoute),
        Map.entry(AbstractionFailed.class, this::processAbstractionFailedRoute),
        Map.entry(CounterexampleAnalysisFailed.class, this::processCounterexampleAnalysisFailedRoute),
        Map.entry(ValidCounterexample.class, this::processValidCounterexampleRoute),
        Map.entry(InvalidCounterexample.class, this::processInvalidCounterexampleRoute),
        Map.entry(VerificationResult.class, this::processVerificationResultRoute)
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
