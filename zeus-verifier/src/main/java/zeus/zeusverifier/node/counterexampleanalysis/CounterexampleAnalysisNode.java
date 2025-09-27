package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.formula.Formula;
import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractionFailed;
import zeus.shared.message.payload.counterexampleanalysis.AnalyzeCounterExampleRequest;
import zeus.shared.message.payload.counterexampleanalysis.CounterexampleAnalysisFailed;
import zeus.shared.message.payload.counterexampleanalysis.InvalidCounterexample;
import zeus.shared.message.payload.counterexampleanalysis.ValidCounterexample;
import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.message.payload.modelchecking.StopModelCheckingTaskRequest;
import zeus.shared.message.payload.modelchecking.StopModelCheckingTaskRequestStatus;
import zeus.shared.message.payload.modelchecking.SynchronizedCodeModule;
import zeus.shared.message.payload.storage.AddPredicatesRequest;
import zeus.shared.message.payload.storage.AddPredicatesResponse;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class CounterexampleAnalysisNode extends Node<CounterExampleAnalysisNodeConfig> {
  ConcurrentHashMap<UUID, ClientCodeModule> codeModules;
  ConcurrentHashMap<UUID, CompletableFuture<Set<Predicate>>> pendingAddPredicatesRequests;

  public CounterexampleAnalysisNode(CounterExampleAnalysisNodeConfig config) {
    super(config);
    this.codeModules = new ConcurrentHashMap<>();
    this.pendingAddPredicatesRequests = new ConcurrentHashMap<>();
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

  Set<Predicate> addPredicates(UUID verificationUuid, Set<Formula> formulas) {
    UUID uuid = UUID.randomUUID();
    CompletableFuture<Set<Predicate>> completableFuture = new CompletableFuture<>();
    this.pendingAddPredicatesRequests.put(uuid, completableFuture);

    this.sendMessage(new Message<>(new AddPredicatesRequest(
      uuid,
      verificationUuid,
      this.getUuid(),
      formulas
    ), new Recipient(NodeType.STORAGE, NodeSelection.ANY)));

    try {
      return completableFuture.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private RouteResult processAddPredicatesResponseRoute(Message<AddPredicatesResponse> message, Socket socket) {
    System.out.println("Running processAddPredicatesResponseRoute");
    CompletableFuture<Set<Predicate>> completableFutures = this.pendingAddPredicatesRequests.get(message.getPayload().requestUuid());

    if (completableFutures == null) {
      return new RouteResult(new Message<>(new CounterexampleAnalysisFailed(
        this.getUuid(),
        String.format("no pending add predicates request for uuid \"%s\"", message.getPayload().requestUuid())
      ), new Recipient(NodeType.ROOT)));
    }

    completableFutures.complete(message.getPayload().predicates());
    return new RouteResult();
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
      message.getPayload().verificationUuid(),
      message.getPayload().path(),
      codeModule,
      this
    );

    Optional<CounterexampleAnalysisResult> counterexampleOptional = counterexampleAnalyzer.analyze();

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

    CounterexampleAnalysisResult counterexampleAnalysisResult = counterexampleOptional.get();
    Optional<Path> counterExamplePivotPathOptional = counterexampleAnalysisResult.getPivotPath();

    return counterExamplePivotPathOptional.map(path -> new RouteResult(new Message<>(
      new InvalidCounterexample(
        message.getPayload().verificationUuid(),
        message.getPayload().modelCheckingTaskUuid(),
        counterexampleAnalysisResult.getPath(),
        path
      ),
      new Recipient(NodeType.ROOT)
    ))).orElseGet(() -> new RouteResult(new Message<>(
      new ValidCounterexample(
        message.getPayload().verificationUuid(),
        message.getPayload().modelCheckingTaskUuid(),
        counterexampleAnalysisResult.getPath(),
        counterexampleAnalysisResult.getVariableAssignments().orElse(new HashSet<>())
      ),
      new Recipient(NodeType.ROOT)
    )));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::processClientCodeModuleRoute,
        AnalyzeCounterExampleRequest.class, this::processAnalyzeCounterexampleRequestRoute,
        AddPredicatesResponse.class, this::processAddPredicatesResponseRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
