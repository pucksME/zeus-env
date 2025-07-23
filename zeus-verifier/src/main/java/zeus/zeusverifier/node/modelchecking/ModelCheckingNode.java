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
import java.util.UUID;
import java.util.concurrent.*;

public class ModelCheckingNode extends Node<ModelCheckingNodeConfig> {
  ClientCodeModule codeModule;
  ConcurrentHashMap<UUID, CompletableFuture<AbstractLiteral>> pendingAbstractionRequests;
  ExecutorService modelCheckingExecutor;

  public ModelCheckingNode(ModelCheckingNodeConfig config) {
    super(config);
    this.pendingAbstractionRequests = new ConcurrentHashMap<>();
    this.modelCheckingExecutor = Executors.newSingleThreadExecutor();
  }

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket socket) {
    System.out.println("Running processClientCodeModuleRoute route");
    this.codeModule = message.getPayload();
    return new RouteResult(new Message<>(new SetCodeModuleResponse()));
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

  private RouteResult processStartModelCheckingRequestRoute(
    Message<StartModelCheckingRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running startModelChecking route");

    CodeModuleModelChecker codeModuleModelChecker = new CodeModuleModelChecker(this.codeModule, this);
    if (!codeModuleModelChecker.calibrate(message.getPayload().path())) {
      return new RouteResult(new Message<>(new CalibrationFailed(
        this.getUuid(),
        message.getPayload().path()
      ), new Recipient(NodeType.ROOT)), NodeAction.NONE);
    }

    Optional<Path> pathOptional = codeModuleModelChecker.check();

    if (pathOptional.isEmpty()) {
      return new RouteResult();
    }

    Path path = pathOptional.get();

    if (path.states().size() == 1 && path.states().getFirst().getLocation().equals(new Location(-1, -1))) {
      return new RouteResult(new Message<>(new NoCounterexampleFound(this.getUuid()), new Recipient(NodeType.ROOT)));
    }

    return new RouteResult(new Message<>(new AnalyzeCounterExampleRequest(
      this.getUuid(),
      pathOptional.get(),
      message.getPayload().predicates()
    ), new Recipient(NodeType.COUNTEREXAMPLE_ANALYSIS_GATEWAY)));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::processClientCodeModuleRoute,
        StartModelCheckingRequest.class, this::processStartModelCheckingRequestRoute,
        AbstractResponse.class, this::processAbstractResponseRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
