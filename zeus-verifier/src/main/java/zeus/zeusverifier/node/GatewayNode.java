package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.RegisterNodeResponse;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GatewayNode<T extends GatewayNodeConfig> extends Node<T>{
  ConcurrentHashMap<UUID, Socket> nodes;
  ExecutorService nodesExecutorService;
  NodeType gatewayTo;

  public GatewayNode(T config) {
    super(config);
    this.nodes = new ConcurrentHashMap<>();
    this.nodesExecutorService = Executors.newCachedThreadPool();
  }

  public GatewayNode(T config, NodeType gatewayTo) {
    this(config);
    this.gatewayTo = gatewayTo;
  }

  public abstract NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException;

  public void startGatewayServer() throws IOException {
    Optional<Integer> portOptional = Main.parsePort(this.getConfig().getPort());

    if (portOptional.isEmpty()) {
      System.out.printf("could not start node: invalid port \"%s\"%n", this.getConfig().getPort());
      return;
    }

    int port = portOptional.get();
    System.out.printf("Gateway server running at port %s%n", port);

    try (
      ServerSocket serverSocket = new ServerSocket(port);
      ExecutorService executorService = Executors.newCachedThreadPool()
    ) {
      while (!serverSocket.isClosed()) {
        Socket requestSocket = serverSocket.accept();
        Optional<Message<Object>> messageOptional = this.getMessage(requestSocket);
        this.processRequest(
          requestSocket,
          messageOptional,
          this.nodesExecutorService,
          (Message<?> message, Socket socket) -> {
            try {
              return this.handleGatewayServerRequest(message, socket);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
        });
      }
    }
  }

  public UUID registerNode(Socket nodeSocket, Map<UUID, Socket> nodes, ExecutorService nodesExecutorService) throws IOException {
    UUID uuid = UUID.randomUUID();
    nodes.put(uuid, nodeSocket);

    nodesExecutorService.submit(() -> {
      try {
        this.processRequests(nodeSocket, nodesExecutorService, (Message<?> message, Socket socket) -> {
          try {
            return this.handleGatewayServerRequest(message, socket);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    return uuid;
  }

  public RouteResult registerNodeRoute(Message<RegisterNode> message, Socket requestSocket) {
    System.out.println("Running registerNode route");
    UUID uuid = null;
    try {
      uuid = this.registerNode(requestSocket, this.nodes, this.nodesExecutorService);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new RouteResult(new Message<>(new RegisterNodeResponse(uuid)));
  }

  public Optional<UUID> sendMessageToNode(Message<?> message) {
    if (this.nodes.isEmpty()) {
      return Optional.empty();
    }

    List<UUID> nodeUuids = new ArrayList<>(this.nodes.keySet());
    UUID nodeUuid = nodeUuids.get(new Random().nextInt(nodeUuids.size()));
    this.sendMessage(message, this.nodes.get(nodeUuid));
    return Optional.of(nodeUuid);
  }

  public void sendBroadcastMessage(Message<?> message) {
    this.nodes.forEach((UUID _, Socket socket) -> this.sendMessage(message, socket));
  }

  public ConcurrentHashMap<UUID, Socket> getNodes() {
    return nodes;
  }

  public NodeType getGatewayTo() {
    return gatewayTo;
  }
}
