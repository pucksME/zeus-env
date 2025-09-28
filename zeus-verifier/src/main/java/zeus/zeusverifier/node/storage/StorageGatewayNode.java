package zeus.zeusverifier.node.storage;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNodeRequest;
import zeus.shared.message.payload.abstraction.AbstractLiteral;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedRequest;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedResponse;
import zeus.shared.message.payload.storage.GetAbstractLiteralRequest;
import zeus.shared.message.payload.storage.GetAbstractLiteralResponse;
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
  private final ConcurrentHashMap<UUID, Set<CompletableFuture<Optional<AbstractLiteral>>>> pendingGetAbstractLiteralRequests;

  public StorageGatewayNode(StorageGatewayNodeConfig config) {
    super(config, NodeType.STORAGE);
    this.pendingCheckIfComponentVisitedRequests = new ConcurrentHashMap<>();
    this.pendingGetAbstractLiteralRequests = new ConcurrentHashMap<>();
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

  private RouteResult processGetAbstractLiteralRequestRoute(
    Message<GetAbstractLiteralRequest> message,
    Socket socket
  ) {
    System.out.println("Running processGetAbstractLiteralRequestRoute");
    this.pendingGetAbstractLiteralRequests.put(
      message.getPayload().uuid(),
      this.getCompletableFuturesForAllNodes()
    );

    this.sendMessage(new Message<>(new GetAbstractLiteralRequest(
      message.getPayload().uuid(),
      message.getPayload().verificationUuid(),
      message.getPayload().abstractionNodeUuid(),
      message.getPayload().valuations()
    ), new Recipient(NodeType.STORAGE, NodeSelection.ALL)));

    Optional<AbstractLiteral> abstractValueOptional = Optional.empty();

    for (CompletableFuture<Optional<AbstractLiteral>> completableFuture : this.pendingGetAbstractLiteralRequests.get(
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

    return new RouteResult(new Message<>(new GetAbstractLiteralResponse(
      message.getPayload().uuid(),
      abstractValueOptional.orElse(null)
    ), new Recipient(NodeType.ABSTRACTION, message.getPayload().abstractionNodeUuid())));
  }

  private RouteResult processGetAbstractLiteralResponseRoute(
    Message<GetAbstractLiteralResponse> message,
    Socket socket
  ) {
    System.out.println("Running processGetAbstractLiteralResponseRoute");
    Set<CompletableFuture<Optional<AbstractLiteral>>> completableFutures = this.pendingGetAbstractLiteralRequests.get(
      message.getPayload().getRequestUuid()
    );

    if (completableFutures == null) {
      return new RouteResult(NodeAction.TERMINATE);
    }

    for (CompletableFuture<Optional<AbstractLiteral>> completableFuture : completableFutures) {
      if (!completableFuture.isDone()) {
        completableFuture.complete(message.getPayload().getAbstractLiteral());
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
        RegisterNodeRequest.class, this::registerNodeRoute,
        CheckIfComponentVisitedResponse.class, this::processCheckIfComponentVisitedResponseRoute,
        GetAbstractLiteralResponse.class, this::processGetAbstractLiteralResponseRoute
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
        GetAbstractLiteralRequest.class, this::processGetAbstractLiteralRequestRoute
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
