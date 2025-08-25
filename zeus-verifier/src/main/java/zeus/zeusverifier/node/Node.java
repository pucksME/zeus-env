package zeus.zeusverifier.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import zeus.shared.formula.Formula;
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
import zeus.zeusverifier.node.counterexampleanalysis.CounterexampleAnalysisGatewayNode;
import zeus.zeusverifier.node.counterexampleanalysis.CounterexampleAnalysisNode;
import zeus.zeusverifier.node.modelchecking.ModelCheckingGatewayNode;
import zeus.zeusverifier.node.modelchecking.ModelCheckingNode;
import zeus.zeusverifier.node.storage.StorageGatewayNode;
import zeus.zeusverifier.node.storage.StorageNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.*;
import java.net.Socket;
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
  BufferedReader gatewayNodeBufferedReader;

  public Node(T config) {
    this.config = config;
  }

  private NodeType getNodeType() {
    return switch (this.config.getNodeType()) {
      case ROOT_NODE -> NodeType.ROOT;
      case MODEL_CHECKING_GATEWAY_NODE -> NodeType.MODEL_CHECKING_GATEWAY;
      case MODEL_CHECKING_NODE -> NodeType.MODEL_CHECKING;
      case ABSTRACTION_GATEWAY_NODE -> NodeType.ABSTRACTION_GATEWAY;
      case ABSTRACTION_NODE -> NodeType.ABSTRACTION;
      case COUNTER_EXAMPLE_GATEWAY_NODE -> NodeType.COUNTEREXAMPLE_ANALYSIS_GATEWAY;
      case COUNTER_EXAMPLE_NODE -> NodeType.COUNTEREXAMPLE_ANALYSIS;
      case STORAGE_GATEWAY_NODE -> NodeType.STORAGE_GATEWAY;
      case STORAGE_NODE -> NodeType.STORAGE;
    };
  }

  public abstract NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException;

  protected <T> Optional<Message<T>> parseMessage(String message) {
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Message.class, new MessageJsonDeserializer<Message<T>>())
      .registerTypeAdapter(Type.class, new ObjectJsonDeserializer<Type>())
      .registerTypeAdapter(BodyComponent.class, new ObjectJsonDeserializer<BodyComponent>())
      .registerTypeAdapter(Expression.class, new ObjectJsonDeserializer<Expression>())
      .registerTypeAdapter(Formula.class, new ObjectJsonDeserializer<Formula>())
      .create();

    try {
      return Optional.ofNullable(gson.fromJson(message, Message.class));
    } catch (JsonParseException jsonParseException) {
      System.out.printf("Could not deserialize \"%s\": parsing failed%n", message);
      return Optional.empty();
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  protected <T> Optional<Message<T>> getMessage(BufferedReader bufferedReader) throws IOException {
    String message = MessageUtils.readMessage(bufferedReader);
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
  }

  public static synchronized void sendMessage(Message<?> message, Socket socket) {
    try {
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println(message.toJsonString());
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public void sendMessageToGateway(Message<?> message) {
    Node.sendMessage(message, this.gatewayNodeSocket);
  }

  public void sendMessage(Message<?> message) {
    if (message.getRecipient().isEmpty()) {
      System.out.println("Could not send message: no receiver specified");
      return;
    }
    this.sendMessageToGateway(message);
  }

  private boolean registerOnGateway(Socket gatewaySocket, BufferedReader bufferedReader) throws IOException {
    Node.sendMessage(new Message<>(new RegisterNode(switch (this) {
      case ModelCheckingGatewayNode _ -> NodeType.MODEL_CHECKING_GATEWAY;
      case ModelCheckingNode _ -> NodeType.MODEL_CHECKING;
      case AbstractionGatewayNode _ -> NodeType.ABSTRACTION_GATEWAY;
      case AbstractionNode _ -> NodeType.ABSTRACTION;
      case CounterexampleAnalysisGatewayNode _ -> NodeType.COUNTEREXAMPLE_ANALYSIS_GATEWAY;
      case CounterexampleAnalysisNode _ -> NodeType.COUNTEREXAMPLE_ANALYSIS;
      case StorageGatewayNode _ -> NodeType.STORAGE_GATEWAY;
      case StorageNode _ -> NodeType.STORAGE;
      default -> throw new RuntimeException(String.format(
        "Could not register node on gateway: unsupported node type \"%s\"",
        this.getClass().getSimpleName()
      ));
    })), gatewaySocket);

    Optional<Message<RegisterNodeResponse>> messageOptional = this.parseMessage(
      MessageUtils.readMessage(bufferedReader)
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
      switch (recipient.getNodeType()) {
        case ROOT:
          message.removeRecipient();
          return false;
        case MODEL_CHECKING_GATEWAY:
        case MODEL_CHECKING:
          Node.sendMessage(message, ((RootNode) this).modelCheckingGatewayNodeSocket);
          break;
        case ABSTRACTION_GATEWAY:
        case ABSTRACTION:
          Node.sendMessage(message, ((RootNode) this).abstractionGatewayNodeSocket);
          break;
        case COUNTEREXAMPLE_ANALYSIS_GATEWAY:
        case COUNTEREXAMPLE_ANALYSIS:
          Node.sendMessage(message, ((RootNode) this).counterexampleAnalysisGatewayNodeSocket);
          break;
        case STORAGE_GATEWAY:
        case STORAGE:
          Node.sendMessage(message, ((RootNode) this).storageGatewayNodeSocket);
          break;
      }
      return true;
    }

    if (this instanceof zeus.zeusverifier.node.GatewayNode) {
      if (recipient.getNodeType() == this.getNodeType()) {
        message.removeRecipient();
        return false;
      }

      if (recipient.getNodeType() == ((zeus.zeusverifier.node.GatewayNode<?>) this).getGatewayTo()) {
        if (recipient.getNodeUuid().isPresent()) {
          Socket socket = ((zeus.zeusverifier.node.GatewayNode<?>) this).nodes.get(recipient.getNodeUuid().get());

          if (socket == null) {
            System.out.printf(
              "Could not send message to recipient: node \"%s\" does not exist%n",
              recipient.getNodeUuid().get()
            );

            this.terminate();
            return false;
          }

          Node.sendMessage(message, socket);
          return true;
        }

        switch (recipient.getNodeSelection()) {
          case ANY -> ((zeus.zeusverifier.node.GatewayNode<?>) this).sendMessageToNode(message);
          case ALL -> ((zeus.zeusverifier.node.GatewayNode<?>) this).sendBroadcastMessage(message);
        }
        return true;
      }
    }

    Optional<Socket> gatewayNodeSocketOptional = this.getGatewayNodeSocket();
    if (gatewayNodeSocketOptional.isPresent() && message.getRecipient().get().getNodeType() != this.getNodeType()) {
      this.sendMessageToGateway(message);
      return true;
    }

    message.removeRecipient();
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
    BufferedReader bufferedReader,
    ExecutorService executorService,
    BiFunction<Message<?>, Socket, NodeAction> requestHandler
  ) throws IOException, RejectedExecutionException {
    while (true) {
      this.processRequest(socket, this.getMessage(bufferedReader), executorService, requestHandler);
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
      this.gatewayNodeSocket = socket;
      this.gatewayNodeBufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

      if (!this.registerOnGateway(this.gatewayNodeSocket, this.gatewayNodeBufferedReader)) {
        return;
      }

      this.processRequests(socket, this.gatewayNodeBufferedReader, executorService, (Message<?> message, Socket messageSocket) -> {
        try {
          return this.handleGatewayRequest(message, messageSocket);
        } catch (Exception e) {
          e.printStackTrace();
          throw new RuntimeException(e);
        }
      });
    }
  }

  protected NodeAction processMessage(
    Message message,
    Socket requestSocket,
    Map<Class<?>, BiFunction<Message, Socket, RouteResult>> routes
  ) {
    Class<?> payloadClass = message.getPayload().getClass();
    BiFunction<Message, Socket, RouteResult> route = routes.get(payloadClass);

    if (route == null) {
      System.out.printf("Warning: processed message with unsupported route \"%s\"%n", payloadClass.getSimpleName());
      return NodeAction.TERMINATE;
    }

    RouteResult routeResult = route.apply(message, requestSocket);
    Optional<Message<?>> responseMessageOptional = routeResult.getResponseMessage();

    if (responseMessageOptional.isPresent()) {
      Message<?> responseMessage = responseMessageOptional.get();
      if (this.handleMessageWithRecipient(responseMessage)) {
        return routeResult.getNodeAction();
      }
      this.sendMessage(responseMessage, requestSocket);
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
