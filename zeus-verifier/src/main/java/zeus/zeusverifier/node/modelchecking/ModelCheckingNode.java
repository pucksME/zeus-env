package zeus.zeusverifier.node.modelchecking;

import zeus.shared.formula.Formula;
import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.shared.message.payload.abstraction.AbstractResponse;
import zeus.shared.message.payload.abstraction.AbstractLiteral;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleRequest;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.message.payload.storage.AddVisitedComponentRequest;
import zeus.shared.message.payload.storage.AddVisitedComponentResponse;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedRequest;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedResponse;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

public class ModelCheckingNode extends Node<ModelCheckingNodeConfig> {
  ConcurrentHashMap<UUID, ClientCodeModule> codeModules = new ConcurrentHashMap<>();
  ConcurrentHashMap<UUID, CompletableFuture<AbstractLiteral>> pendingAbstractionRequests;
  ConcurrentHashMap<UUID, CompletableFuture<Boolean>> pendingCheckIfComponentVisitedRequests;
  ConcurrentHashMap<UUID, CompletableFuture<Boolean>> pendingAddVisitedComponentRequests;
  ExecutorService modelCheckingExecutor;

  public ModelCheckingNode(ModelCheckingNodeConfig config) {
    super(config);
    this.pendingAbstractionRequests = new ConcurrentHashMap<>();
    this.pendingCheckIfComponentVisitedRequests = new ConcurrentHashMap<>();
    this.pendingAddVisitedComponentRequests = new ConcurrentHashMap<>();
    this.modelCheckingExecutor = Executors.newSingleThreadExecutor();
  }

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket socket) {
    System.out.println("Running processClientCodeModuleRoute route");
    Optional<UUID> verificationUuidOptional = message.getPayload().getVerificationUuid();

    if (verificationUuidOptional.isEmpty()) {
      return new RouteResult(new Message<>(
        new ModelCheckingFailed(this.getUuid(), "missing verification uuid in client code module"),
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

  CompletableFuture<AbstractLiteral> sendAbstractRequest(
    Map<UUID, Predicate> predicates,
    Map<UUID, PredicateValuation> predicateValuations,
    Formula expression
  ) {
    System.out.println("Running sendAbstractRequest route");
    UUID uuid = UUID.randomUUID();
    this.sendMessage(new Message<>(
      new AbstractRequest(uuid, predicates, predicateValuations, expression),
      new Recipient(NodeType.ABSTRACTION, NodeSelection.ANY)
    ));
    CompletableFuture<AbstractLiteral> completableFuture = new CompletableFuture<>();
    this.pendingAbstractionRequests.put(uuid, completableFuture);
    return completableFuture;
  }

  private RouteResult processAbstractResponseRoute(Message<AbstractResponse> message, Socket requestSocket) {
    System.out.println("Running setAbstractResponse route");
    CompletableFuture<AbstractLiteral> completableFuture = this.pendingAbstractionRequests.get(
      message.getPayload().uuid()
    );

    if (completableFuture == null) {
      System.out.printf(
        "Could not handle abstract response: no pending abstract request for uuid \"%s\"%n",
        message.getPayload().uuid()
      );
      return new RouteResult(NodeAction.TERMINATE);
    }

    this.pendingAbstractionRequests.remove(message.getPayload().uuid());
    completableFuture.complete(message.getPayload().abstractLiteral());
    return new RouteResult();
  }

  Optional<Boolean> checkIfComponentVisited(
    UUID verificationUuid,
    Location location,
    Set<PredicateValuation> predicateValuations) {
    UUID uuid = UUID.randomUUID();

    this.sendMessage(new Message<>(new CheckIfComponentVisitedRequest(
      uuid,
      verificationUuid,
      this.getUuid(),
      location,
      predicateValuations
    ), new Recipient(NodeType.STORAGE_GATEWAY)));

    CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
    this.pendingCheckIfComponentVisitedRequests.put(uuid, completableFuture);

    try {
      return Optional.of(completableFuture.get());
    } catch (InterruptedException | ExecutionException e) {
      return Optional.empty();
    }
  }

  boolean addVisitedComponent(UUID verificationUuid, Location location, Set<PredicateValuation> predicateValuations) {
    UUID uuid = UUID.randomUUID();

    this.sendMessage(new Message<>(new AddVisitedComponentRequest(
      uuid,
      verificationUuid,
      this.getUuid(),
      location,
      predicateValuations
    ), new Recipient(NodeType.STORAGE, NodeSelection.ANY)));

    CompletableFuture<Boolean> completableFuture = new CompletableFuture<>();
    this.pendingAddVisitedComponentRequests.put(uuid, completableFuture);

    try {
      return completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private RouteResult processStartModelCheckingRequestRoute(
    Message<StartModelCheckingTaskRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running startModelChecking route");
    ClientCodeModule codeModule = this.codeModules.get(message.getPayload().getVerificationUuid());

    if (codeModule == null) {
      return new RouteResult(new Message<>(
        new ModelCheckingFailed(
          this.getUuid(),
          String.format("missing code module for verification uuid \"%s\"", message.getPayload().getVerificationUuid())
        ),
        new Recipient(NodeType.ROOT)
      ), NodeAction.TERMINATE);
    }

    CodeModuleModelChecker codeModuleModelChecker = new CodeModuleModelChecker(
      message.getPayload().getVerificationUuid(),
      codeModule,
      this
    );

    if (!codeModuleModelChecker.calibrate(message.getPayload())) {
      return new RouteResult(new Message<>(new CalibrationFailed(
        this.getUuid(),
        message.getPayload().getPath()
      ), new Recipient(NodeType.ROOT)), NodeAction.TERMINATE);
    }

    ModelCheckingResult modelCheckingResult = codeModuleModelChecker.check();

    return switch (modelCheckingResult.getStatus()) {
      case NO_COUNTEREXAMPLE_FOUND -> new RouteResult(new Message<>(
        new StopModelCheckingTaskRequest(
          message.getPayload().getVerificationUuid(),
          message.getPayload().getUuid(),
          StopModelCheckingTaskRequestStatus.NO_COUNTEREXAMPLE_FOUND
        ),
        new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
      ));

      case INFEASIBLE_PREDICATE_VALUATIONS -> new RouteResult(new Message<>(
          new StopModelCheckingTaskRequest(
            message.getPayload().getVerificationUuid(),
            message.getPayload().getUuid(),
            StopModelCheckingTaskRequestStatus.INFEASIBLE_PATH
          ),
          new Recipient(NodeType.MODEL_CHECKING_GATEWAY)
        ));

      case UNSUPPORTED_COMPONENT -> new RouteResult(new Message<>(
          new ModelCheckingFailed(this.getUuid(), "unsupported component"),
          new Recipient(NodeType.ROOT)
        ));

      case ABSTRACTION_FAILED -> new RouteResult(new Message<>(
          new ModelCheckingFailed(this.getUuid(), "abstraction failed"),
          new Recipient(NodeType.ROOT)
        ));

      case COMPONENT_ALREADY_VISITED -> new RouteResult(new Message<>(
        new StopModelCheckingTaskRequest(
          message.getPayload().getVerificationUuid(),
          message.getPayload().getUuid(),
          StopModelCheckingTaskRequestStatus.COMPONENT_ALREADY_VISITED
        )
      ));

      case CHECK_IF_COMPONENT_VISITED_FAILED -> new RouteResult(new Message<>(
        new ModelCheckingFailed(this.getUuid(), "check if component visited failed"),
        new Recipient(NodeType.ROOT)
      ));

      case OK -> modelCheckingResult.getPath()
        .map(path -> new RouteResult(new Message<>(
          new AnalyzeCounterExampleRequest(
            message.getPayload().getVerificationUuid(),
            message.getPayload().getUuid(),
            this.getUuid(),
            path
          ),
          new Recipient(NodeType.COUNTEREXAMPLE_ANALYSIS_GATEWAY))))
        .orElse(new RouteResult(new Message<>(
          new ModelCheckingFailed(this.getUuid(), "missing path in model checking result"),
          new Recipient(NodeType.ROOT)
        )));
    };

  }

  private RouteResult processCheckIfComponentVisitedResponseRoute(
    Message<CheckIfComponentVisitedResponse> message,
    Socket socket
  ) {
    System.out.println("Running checkIfComponentVisited route");
    CompletableFuture<Boolean> completableFuture = this.pendingCheckIfComponentVisitedRequests.get(message.getPayload().requestUuid());

    if (completableFuture == null) {
      return new RouteResult(new Message<>(new ModelCheckingFailed(
        this.getUuid(),
        String.format("no pending check if component visited for uuid \"%s\"", message.getPayload().requestUuid())
      ), new Recipient(NodeType.ROOT)));
    }

    completableFuture.complete(message.getPayload().visited());
    return new RouteResult();
  }

  private RouteResult processAddVisitedComponentResponseRoute(Message<AddVisitedComponentResponse> message, Socket socket) {
    System.out.println("Running AddVisitedComponentResponseRoute route");
    CompletableFuture<Boolean> completableFuture = this.pendingAddVisitedComponentRequests.get(message.getPayload().requestUuid());

    if (completableFuture == null) {
      return new RouteResult(new Message<>(new ModelCheckingFailed(
        this.getUuid(),
        String.format("no pending add visited component request for uuid \"%s\"", message.getPayload().requestUuid())
      ), new Recipient(NodeType.ROOT)));
    }

    completableFuture.complete(message.getPayload().existed());
    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::processClientCodeModuleRoute,
        StartModelCheckingTaskRequest.class, this::processStartModelCheckingRequestRoute,
        AbstractResponse.class, this::processAbstractResponseRoute,
        CheckIfComponentVisitedResponse.class, this::processCheckIfComponentVisitedResponseRoute,
        AddVisitedComponentResponse.class, this::processAddVisitedComponentResponseRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
