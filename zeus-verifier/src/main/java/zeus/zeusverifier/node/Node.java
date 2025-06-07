package zeus.zeusverifier.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
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
import zeus.zeusverifier.config.GatewayNode;
import zeus.zeusverifier.node.abstraction.AbstractionGatewayNode;
import zeus.zeusverifier.node.abstraction.AbstractionNode;
import zeus.zeusverifier.node.modelchecking.ModelCheckingGatewayNode;
import zeus.zeusverifier.node.modelchecking.ModelCheckingNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.BiFunction;

public abstract class Node<T extends Config> {
  T config;
  UUID uuid;
  Socket gatewayNodeSocket;

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

  protected <T> Optional<Message<T>> getMessage(Socket requestSocket) throws IOException {
    try {
      String message = MessageUtils.readMessage(requestSocket.getInputStream());
      if (message == null) {
        System.out.println("Received empty message: closing socket");
        this.terminate();
        return Optional.empty();
      }

      Optional<Message<T>> messageOptional = this.parseMessage(message);

      if (messageOptional.isEmpty()) {
        System.out.printf("Warning: received invalid message \"%s\"%n", message);
        this.terminate();
      }

      return messageOptional;
    } catch (SocketException socketException) {
      this.terminate();
      return Optional.empty();
    }

  }

  public void sendMessage(Message<?> message, Socket socket) {
    try {
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println(message.toJsonString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void sendMessageToGateway(Message<?> message) {
    this.sendMessage(message, this.gatewayNodeSocket);
  }

  public void sendMessage(Message<?> message) {
    if (message.getRecipient().isEmpty()) {
      System.out.println("Could not send message: no receiver specified");
      return;
    }
    this.sendMessageToGateway(message);
  }

  private boolean registerOnGateway(Socket gatewaySocket) throws IOException {
    PrintWriter printWriter = new PrintWriter(gatewaySocket.getOutputStream(), true);
    printWriter.println(new Message<>(new RegisterNode(switch (this) {
      case ModelCheckingGatewayNode _ -> NodeType.MODEL_CHECKING_GATEWAY;
      case ModelCheckingNode _ -> NodeType.MODEL_CHECKING;
      case AbstractionGatewayNode _ -> NodeType.ABSTRACTION_GATEWAY;
      case AbstractionNode _ -> NodeType.ABSTRACTION;
      default -> throw new RuntimeException(String.format(
        "Could not register node on gateway: unsupported node type \"%s\"",
        this.getClass().getSimpleName()
      ));
    })).toJsonString());

    Optional<Message<RegisterNodeResponse>> messageOptional = this.parseMessage(
      MessageUtils.readMessage(gatewaySocket.getInputStream())
    );

    if (messageOptional.isEmpty()) {
      System.out.println("Could not register node: invalid response message");
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

  public boolean handleMessageWithRecipient(Message<?> message) {
    if (message.getRecipient().isEmpty()) {
      return false;
    }

    Recipient recipient = message.getRecipient().get();

    if (this instanceof RootNode) {
      switch (recipient.nodeType()) {
        case MODEL_CHECKING_GATEWAY:
        case MODEL_CHECKING:
          this.sendMessage(message, ((RootNode) this).modelCheckingGatewayNodeSocket);
          break;
        case ABSTRACTION_GATEWAY:
        case ABSTRACTION:
          this.sendMessage(message, ((RootNode) this).abstractionGatewayNodeSocket);
          break;
      }
      return true;
    }

    if ((recipient.nodeType() == NodeType.MODEL_CHECKING_GATEWAY && this instanceof ModelCheckingGatewayNode) ||
      (recipient.nodeType() == NodeType.ABSTRACTION_GATEWAY && this instanceof AbstractionGatewayNode)) {
      return false;
    }

    if (this instanceof zeus.zeusverifier.node.GatewayNode) {
      if (recipient.nodeType() == ((zeus.zeusverifier.node.GatewayNode<?>) this).getGatewayTo()) {
        switch (recipient.nodeSelection()) {
          case ANY -> ((zeus.zeusverifier.node.GatewayNode<?>) this).sendMessageToNode(message);
          case ALL -> ((zeus.zeusverifier.node.GatewayNode<?>) this).sendBroadcastMessage(message);
        }
        return true;
      }

      Optional<Socket> gatewayNodeSocketOptional = this.getGatewayNodeSocket();
      if (gatewayNodeSocketOptional.isPresent()) {
        this.sendMessageToGateway(message);
      }

      return true;
    }

    return false;
  }

  void processRequest(
    Socket socket,
    Optional<Message<Object>> messageOptional,
    ExecutorService executorService,
    BiFunction<Message<?>, Socket, NodeAction> requestHandler
  ) throws RejectedExecutionException {
    executorService.submit(() -> {
      if (messageOptional.isEmpty()) {
        this.terminate();
        return;
      }

      Message<?> message = messageOptional.get();
      if (this.handleMessageWithRecipient(message)) {
        return;
      }

      NodeAction nodeAction = requestHandler.apply(message, socket);
      if (nodeAction == NodeAction.NONE) {
        return;
      }

      if (nodeAction == NodeAction.TERMINATE) {
        this.terminate();
      }
    });
  }

  void processRequests(
    Socket socket,
    ExecutorService executorService,
    BiFunction<Message<?>, Socket, NodeAction> requestHandler
  ) throws IOException, RejectedExecutionException {
    while (true) {
      this.processRequest(socket, this.getMessage(socket), executorService, requestHandler);
    }
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
      if (!this.registerOnGateway(socket)) {
        return;
      }

      this.gatewayNodeSocket = socket;
      this.processRequests(socket, executorService, (Message<?> message, Socket messageSocket) -> {
        try {
          return this.handleGatewayRequest(message, messageSocket);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      });
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

  public void terminate() {
    System.exit(1);
  }

  public T getConfig() {
    return config;
  }

  public UUID getUuid() {
    return uuid;
  }

  public Optional<Socket> getGatewayNodeSocket() {
    return Optional.ofNullable(gatewayNodeSocket);
  }
}
