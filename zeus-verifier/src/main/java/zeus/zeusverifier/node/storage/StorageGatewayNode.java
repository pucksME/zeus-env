package zeus.zeusverifier.node.storage;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedRequest;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedResponse;
import zeus.shared.message.payload.storage.CheckPredicateValuationsRequest;
import zeus.shared.message.payload.storage.CheckPredicateValuationsResponse;
import zeus.zeusverifier.config.storagenode.StorageGatewayNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StorageGatewayNode extends GatewayNode<StorageGatewayNodeConfig> {
  private final ConcurrentHashMap<UUID, Set<CompletableFuture<Boolean>>> pendingCheckIfComponentVisitedRequests;
  private final ConcurrentHashMap<UUID, Set<CompletableFuture<Optional<Boolean>>>> pendingCheckPredicateValuationsRequests;

  public StorageGatewayNode(StorageGatewayNodeConfig config) {
    super(config, NodeType.STORAGE);
    this.pendingCheckIfComponentVisitedRequests = new ConcurrentHashMap<>();
    this.pendingCheckPredicateValuationsRequests = new ConcurrentHashMap<>();
  }

  private <T> Set<CompletableFuture<T>> getCompletableFuturesForAllNodes() {
    return IntStream
      .range(0, this.getNodes().size())
      .mapToObj(i -> new CompletableFuture<T>()).collect(Collectors.toSet());
  }

  private RouteResult processCheckIfComponentVisitedRequestRoute(
    Message<CheckIfComponentVisitedRequest> message,
    Socket socket
  ) {
    System.out.println("Running processCheckIfComponentVisitedRequestRoute");
    this.pendingCheckIfComponentVisitedRequests.put(
      message.getPayload().uuid(),
      this.getCompletableFuturesForAllNodes()
    );

    this.sendMessage(new Message<>(new CheckIfComponentVisitedRequest(
      message.getPayload().uuid(),
      message.getPayload().verificationUuid(),
      message.getPayload().modelCheckingNodeUuid(),
      message.getPayload().location(),
      message.getPayload().predicateValuations()
    ), new Recipient(NodeType.STORAGE, NodeSelection.ALL)));

    boolean visited = false;
    for (CompletableFuture<Boolean> completableFuture : this.pendingCheckIfComponentVisitedRequests.get(
      message.getPayload().uuid()
    )) {
      try {
        visited = completableFuture.get();
        if (visited) {
          break;
        }
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }

    this.pendingCheckIfComponentVisitedRequests.remove(message.getPayload().uuid());
    return new RouteResult(new Message<>(new CheckIfComponentVisitedResponse(
      message.getPayload().uuid(),
      message.getPayload().location(),
      visited
    ), new Recipient(NodeType.MODEL_CHECKING, message.getPayload().modelCheckingNodeUuid())));
  }

  private RouteResult processCheckIfComponentVisitedResponseRoute(
    Message<CheckIfComponentVisitedResponse> message,
    Socket socket
  ) {
    System.out.println("Running processCheckIfComponentVisitedResponseRoute");
    Set<CompletableFuture<Boolean>> completableFutures = this.pendingCheckIfComponentVisitedRequests.get(
      message.getPayload().requestUuid()
    );

    if (completableFutures == null) {
      return new RouteResult(NodeAction.TERMINATE);
    }

    for (CompletableFuture<Boolean> completableFuture : completableFutures) {
      if (!completableFuture.isDone()) {
        completableFuture.complete(message.getPayload().visited());
        break;
      }
    }

    return new RouteResult();
  }

  private RouteResult processCheckPredicateValuationsRequestRoute(
    Message<CheckPredicateValuationsRequest> message,
    Socket socket
  ) {
    System.out.println("Running processCheckPredicateValuationsRoute");
    this.pendingCheckPredicateValuationsRequests.put(
      message.getPayload().uuid(),
      this.getCompletableFuturesForAllNodes()
    );

    this.sendMessage(new Message<>(new CheckPredicateValuationsRequest(
      message.getPayload().uuid(),
      message.getPayload().verificationUuid(),
      message.getPayload().abstractionNodeUuid(),
      message.getPayload().predicateValuations()
    ), new Recipient(NodeType.STORAGE, NodeSelection.ALL)));

    Optional<Boolean> abstractValueOptional = Optional.empty();

    for (CompletableFuture<Optional<Boolean>> completableFuture : this.pendingCheckPredicateValuationsRequests.get(
      message.getPayload().uuid()
    )) {
      try {
        abstractValueOptional = completableFuture.get();

        if (abstractValueOptional.isPresent()) {
          break;
        }
      } catch (InterruptedException | ExecutionException e) {
        throw new RuntimeException(e);
      }
    }

    return new RouteResult(new Message<>(new CheckPredicateValuationsResponse(
      message.getPayload().uuid(),
      abstractValueOptional.orElse(null)
    ), new Recipient(NodeType.ABSTRACTION, message.getPayload().abstractionNodeUuid())));
  }

  private RouteResult processCheckPredicateValuationsResponseRoute(
    Message<CheckPredicateValuationsResponse> message,
    Socket socket
  ) {
    System.out.println("Running processCheckPredicateValuationsResponseRoute");
    Set<CompletableFuture<Optional<Boolean>>> completableFutures = this.pendingCheckPredicateValuationsRequests.get(
      message.getPayload().getRequestUuid()
    );

    if (completableFutures == null) {
      return new RouteResult(NodeAction.TERMINATE);
    }

    for (CompletableFuture<Optional<Boolean>> completableFuture : completableFutures) {
      if (!completableFuture.isDone()) {
        completableFuture.complete(message.getPayload().getAbstractValue());
        break;
      }
    }

    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNode.class, this::registerNodeRoute,
        CheckIfComponentVisitedResponse.class, this::processCheckIfComponentVisitedResponseRoute,
        CheckPredicateValuationsResponse.class, this::processCheckPredicateValuationsResponseRoute
      )
    );
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        CheckIfComponentVisitedRequest.class, this::processCheckIfComponentVisitedRequestRoute,
        CheckPredicateValuationsRequest.class, this::processCheckPredicateValuationsRequestRoute
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
