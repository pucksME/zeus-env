package zeus.zeusverifier.node.abstraction;

import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.shared.message.payload.abstraction.AbstractResponse;
import zeus.shared.message.payload.abstraction.AbstractionFailed;
import zeus.shared.message.payload.modelchecking.ExpressionValuation;
import zeus.shared.message.payload.storage.GetAbstractLiteralRequest;
import zeus.shared.message.payload.storage.GetAbstractLiteralResponse;
import zeus.zeusverifier.config.abstractionnode.AbstractionNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AbstractionNode extends Node<AbstractionNodeConfig> {
  private final ConcurrentHashMap<UUID, CompletableFuture<RouteResult>> pendingAbstractions;

  public AbstractionNode(AbstractionNodeConfig config) {
    super(config);
    this.pendingAbstractions = new ConcurrentHashMap<>();
  }

  private RouteResult processAbstractRequestRoute(Message<AbstractRequest> message, Socket requestSocket) {
    System.out.printf("Running processAbstractRequestRoute for uuid \"%s\"%n", message.getPayload().uuid());
    this.pendingAbstractions.put(message.getPayload().uuid(), new CompletableFuture<>());

    this.sendMessage(new Message<>(new GetAbstractLiteralRequest(
      message.getPayload().uuid(),
      message.getPayload().verificationUuid(),
      this.getUuid(),
      Stream.concat(
        message.getPayload().predicateValuations().values().stream(),
        Stream.of(new ExpressionValuation(message.getPayload().expressionIdentifier()))
      ).collect(Collectors.toSet())
    ), new Recipient(NodeType.STORAGE_GATEWAY)));

    new Thread(() -> {
      Abstractor abstractor = new Abstractor(message.getPayload().verificationUuid(), this);
      AbstractionResult abstractionResult = abstractor.computeAbstraction(
        message.getPayload().predicates(),
        message.getPayload().predicateValuations(),
        message.getPayload().expression(),
        message.getPayload().expressionIdentifier()
      );

      pendingAbstractions.get(message.getPayload().uuid()).complete(switch (abstractionResult.getStatus()) {
        case MISSING_PREDICATE_VALUATIONS -> new RouteResult(new Message<>(new AbstractionFailed(
          this.getUuid(),
          "missing predicate valuations"
        ), new Recipient(NodeType.ROOT)));

        case OK -> new RouteResult((abstractionResult.getAbstractLiteral().isPresent())
          ? new Message<>(
              new AbstractResponse(
                message.getPayload().uuid(),
                abstractionResult.getAbstractLiteral().get()
              ),
              new Recipient(NodeType.MODEL_CHECKING, message.getPayload().modelCheckingNodeUuid())
            )
          : new Message<>(
              new AbstractionFailed(this.getUuid(),"abstract literal not present"),
              new Recipient(NodeType.ROOT)
            ));
      });
    }).start();

    try {
      RouteResult routeResult = pendingAbstractions.get(message.getPayload().uuid()).get();
      if (routeResult.getResponseMessage().isEmpty()) {
        return routeResult;
      }

      Message<?> responseMessage = routeResult.getResponseMessage().get();

      if (responseMessage.getRecipient().isEmpty()) {
        return routeResult;
      }

      Recipient recipient = responseMessage.getRecipient().get();

      if (recipient.getNodeType() == NodeType.MODEL_CHECKING) {
        recipient.setNodeUuid(message.getPayload().modelCheckingNodeUuid());
      }

      return routeResult;
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  private RouteResult processGetAbstractLiteralResponseRoute(
    Message<GetAbstractLiteralResponse> message,
    Socket socket
  ) {
    System.out.println("Running processGetAbstractLiteralResponseRoute");
    CompletableFuture<RouteResult> completableFuture = this.pendingAbstractions.get(
      message.getPayload().getRequestUuid()
    );

    if (completableFuture == null) {
      return new RouteResult();
    }

    if (message.getPayload().getAbstractLiteral().isEmpty()) {
      return new RouteResult();
    }

    completableFuture.complete(new RouteResult(new Message<>(new AbstractResponse(
      message.getPayload().getRequestUuid(),
      message.getPayload().getAbstractLiteral().get()
    ), new Recipient(NodeType.MODEL_CHECKING))));

    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AbstractRequest.class, this::processAbstractRequestRoute,
        GetAbstractLiteralResponse.class, this::processGetAbstractLiteralResponseRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
