package zeus.zeusverifier.node.storage;

import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.message.payload.storage.AddVisitedComponent;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedRequest;
import zeus.shared.message.payload.storage.CheckIfComponentVisitedResponse;
import zeus.zeusverifier.config.storagenode.StorageNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageNode extends Node<StorageNodeConfig> {
  private final ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Set<Set<PredicateValuation>>>> visitedComponents;

  public StorageNode(StorageNodeConfig config) {
    super(config);
    this.visitedComponents = new ConcurrentHashMap<>();
  }

  private RouteResult processAddVisitedComponentRoute(Message<AddVisitedComponent> message, Socket socket) {
    System.out.println("Running AddVisitedComponentRoute route");
    ConcurrentHashMap<Location, Set<Set<PredicateValuation>>> visitedComponents = this.visitedComponents.get(message.getPayload().verificationUuid());
    if (visitedComponents == null) {
      this.visitedComponents.put(
        message.getPayload().verificationUuid(),
        new ConcurrentHashMap<>(Map.of(
          message.getPayload().location(),
          new HashSet<>(Set.of(message.getPayload().predicateValuations()))
        ))
      );
    }

    this.visitedComponents
      .get(message.getPayload().verificationUuid())
      .get(message.getPayload().location())
      .add(message.getPayload().predicateValuations());

    return new RouteResult();
  }

  private RouteResult processCheckIfComponentVisitedRoute(
    Message<CheckIfComponentVisitedRequest> message,
    Socket socket
  ) {
    System.out.println("Running CheckIfComponentVisitedRoute route");
    ConcurrentHashMap<Location, Set<Set<PredicateValuation>>> visitedComponents = this.visitedComponents.get(
      message.getPayload().verificationUuid()
    );

    if (visitedComponents == null) {
      return new RouteResult(new Message<>(new CheckIfComponentVisitedResponse(
        message.getPayload().uuid(),
        message.getPayload().location(),
        false
      ), new Recipient(NodeType.STORAGE_GATEWAY)));
    }

    Set<Set<PredicateValuation>> predicateValuations = visitedComponents.get(message.getPayload().location());
    return new RouteResult(new Message<>(new CheckIfComponentVisitedResponse(
      message.getPayload().uuid(),
      message.getPayload().location(),
      predicateValuations != null && predicateValuations.contains(message.getPayload().predicateValuations())
    ), new Recipient(NodeType.STORAGE_GATEWAY)));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AddVisitedComponent.class, this::processAddVisitedComponentRoute,
        CheckIfComponentVisitedRequest.class, this::processCheckIfComponentVisitedRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
