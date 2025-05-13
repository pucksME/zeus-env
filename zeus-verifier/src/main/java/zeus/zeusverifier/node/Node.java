package zeus.zeusverifier.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import zeus.shared.message.Message;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.RegisterNodeResponse;
import zeus.shared.message.utils.MessageJsonDeserializer;
import zeus.shared.message.utils.MessageUtils;
import zeus.shared.message.utils.ObjectJsonDeserializer;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.Config;
import zeus.zeusverifier.config.modelcheckingnode.GatewayNode;
import zeus.zeusverifier.node.modelchecking.ModelCheckingGatewayNode;
import zeus.zeusverifier.node.modelchecking.ModelCheckingNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public abstract class Node<T extends Config> {
  T config;
  UUID uuid;

  public Node(T config) {
    this.config = config;
  }

  public abstract NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException;

  protected <T> Optional<Message<T>> parseMessage(String message) {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Message.class, new MessageJsonDeserializer<Message<T>>())
      .registerTypeAdapter(Type.class, new ObjectJsonDeserializer<Type>())
      .registerTypeAdapter(BodyComponent.class, new ObjectJsonDeserializer<BodyComponent>())
      .registerTypeAdapter(Expression.class, new ObjectJsonDeserializer<Expression>())
      .create();

    try {
      return Optional.ofNullable(gson.fromJson(message, Message.class));
    } catch (JsonParseException jsonParseException) {
      System.out.printf("Could not deserialize \"%s\": parsing failed%n", message);
      return Optional.empty();
    }
  }

  protected <T> Optional<Message<T>> getMessage(Socket requestSocket, Closeable serverSocket, ExecutorService executorService) throws IOException {
    String message = MessageUtils.readMessage(requestSocket.getInputStream());

    if (message == null) {
      System.out.println("Received empty message: closing socket");
      requestSocket.close();
      return Optional.empty();
    }

    Optional<Message<T>> messageOptional = this.parseMessage(message);

    if (messageOptional.isEmpty()) {
      System.out.printf("Warning: received invalid message \"%s\"%n", message);
      this.terminate(serverSocket, executorService);
    }

    return messageOptional;
  }

  protected <T> Optional<Message<T>> getMessage(Socket requestSocket, ExecutorService executorService) throws IOException {
    return this.getMessage(requestSocket, null, executorService);
  }

  public void sendMessage(Message<?> message, Socket socket) {
    try {
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println(message.toJsonString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean registerOnGateway(Socket gatewaySocket) throws IOException {
    PrintWriter printWriter = new PrintWriter(gatewaySocket.getOutputStream(), true);
    printWriter.println(new Message<>(new RegisterNode(switch (this) {
      case ModelCheckingGatewayNode _ -> NodeType.MODEL_CHECKING_GATEWAY;
      case ModelCheckingNode _ -> NodeType.MODEL_CHECKING;
      default -> throw new RuntimeException(String.format(
        "Could not register node on gateway: unsupported node type \"%s\"",
        this.getClass().getSimpleName()
      ));
    })).toJsonString());

    Optional<Message<RegisterNodeResponse>> messageOptional = this.parseMessage(
      MessageUtils.readMessage(gatewaySocket.getInputStream())
    );

    if (messageOptional.isEmpty()) {
      System.out.println("Could not node: invalid response message");
      gatewaySocket.close();
      return false;
    }

    this.uuid = messageOptional.get().getPayload().uuid();

    System.out.printf(
      "Successfully registered node \"%s\"%n",
      this.uuid
    );
    return true;
  }

  public void startGatewayListener(GatewayNode gatewayNode) throws IOException {
    String rootNodePort = gatewayNode.getPort();
    Optional<Integer> rootNodePortOptional = Main.parsePort(rootNodePort);

    if (rootNodePortOptional.isEmpty()) {
      throw new RuntimeException(String.format(
        "Could not register model checking node: invalid root node port \"%s\"",
        rootNodePort
      ));
    }

    try (
      Socket socket = new Socket(gatewayNode.getHost(), rootNodePortOptional.get());
      ExecutorService executorService = Executors.newCachedThreadPool()
    ) {
      this.registerOnGateway(socket);
      while (true) {
        Optional<Message<Object>> messageOptional = this.getMessage(socket, executorService);
        executorService.submit(() -> {
          try {
            if (messageOptional.isEmpty()) {
              return;
            }

            NodeAction nodeAction = this.handleGatewayRequest(messageOptional.get(), socket);

            if (nodeAction == NodeAction.NONE) {
              return;
            }

            if (nodeAction == NodeAction.TERMINATE) {
              this.terminate(socket, executorService);
            }
          } catch (IOException ioException) {
            System.out.println("Gateway node became unavailable: stopping node");
            try {
              this.terminate(socket, executorService);
            } catch (IOException terminateIoException) {
              throw new RuntimeException(terminateIoException);
            }
          }
        });
      }
    }
  }

  protected NodeAction processMessage(
    Message message,
    Socket requestSocket,
    Map<Class<?>, BiFunction<Message, Socket, RouteResult>> routes
  ) throws IOException {
    Class<?> payloadClass = message.getPayload().getClass();
    BiFunction<Message, Socket, RouteResult> route = routes.get(payloadClass);

    if (route == null) {
      System.out.printf("Warning: processed message with unsupported route \"%s\"%n", payloadClass);
      return NodeAction.TERMINATE;
    }

    RouteResult routeResult = route.apply(message, requestSocket);
    Optional<Message<?>> responseMessageOptional = routeResult.getResponseMessage();

    if (responseMessageOptional.isPresent()) {
      PrintWriter printWriter = new PrintWriter(requestSocket.getOutputStream(), true);
      printWriter.println(responseMessageOptional.get().toJsonString());
    }

    return routeResult.getNodeAction();
  }

  public abstract void start() throws IOException;

  public void terminate(Closeable socket, ExecutorService executorService) throws IOException {
    if (socket != null) {
      socket.close();
    }

    executorService.shutdown();
  }

  public T getConfig() {
    return config;
  }

  public UUID getUuid() {
    return uuid;
  }
}
