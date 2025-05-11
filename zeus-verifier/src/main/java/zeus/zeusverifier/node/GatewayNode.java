package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.RegisterNodeResponse;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class GatewayNode<T extends GatewayNodeConfig> extends Node<T>{
  ConcurrentHashMap<UUID, Socket> nodes;
  ExecutorService nodesExecutorService;

  public GatewayNode(T config) {
    super(config);
    this.nodes = new ConcurrentHashMap<>();
    this.nodesExecutorService = Executors.newCachedThreadPool();
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
      while (true) {
        Socket requestSocket = serverSocket.accept();
        executorService.submit(() -> {
          try {
            Optional<Message<Object>> messageOptional = this.getMessage(requestSocket, serverSocket, executorService);

            if (messageOptional.isEmpty()) {
              return;
            }

            NodeAction nodeAction = this.handleGatewayServerRequest(messageOptional.get(), requestSocket);

            if (nodeAction == NodeAction.NONE) {
              return;
            }

            if (nodeAction == NodeAction.TERMINATE) {
              this.terminate(serverSocket, executorService);
            }
          } catch (IOException ioException) {
            ioException.printStackTrace();

            try {
              this.terminate(serverSocket, executorService);
            } catch (IOException terminateIoException) {
              throw new RuntimeException(terminateIoException);
            }
          }
        });
      }
    }
  }

  public UUID registerNode(Socket nodeSocket, Map<UUID, Socket> nodes, ExecutorService nodesExecutorService) {
    UUID uuid = UUID.randomUUID();
    nodes.put(uuid, nodeSocket);

    nodesExecutorService.submit(() -> {
      while (true) {
        try {
          Optional<Message<Object>> messageOptional = this.getMessage(nodeSocket, nodesExecutorService);

          if (messageOptional.isEmpty()) {
            return;
          }

          this.handleGatewayServerRequest(messageOptional.get(), nodeSocket);
        } catch (IOException e) {
          System.out.printf("Model checking node \"%s\" became unavailable: removing node%n", uuid);
          nodes.remove(uuid);
          return;
        }
      }
    });

    return uuid;
  }

  public RouteResult registerNodeRoute(
    Message<RegisterNode> message,
    Socket requestSocket
  ) {
    System.out.println("Running registerNode route");
    UUID uuid = this.registerNode(requestSocket, this.nodes, this.nodesExecutorService);
    return new RouteResult(new Message<>(new RegisterNodeResponse(uuid)));
  }

  public void sendBroadcastMessage(Message<?> message) {
    this.nodes.forEach((UUID uuid, Socket socket) -> this.sendMessage(message, socket));
  }

  public ConcurrentHashMap<UUID, Socket> getNodes() {
    return nodes;
  }

  public ExecutorService getNodesExecutorService() {
    return nodesExecutorService;
  }
}
