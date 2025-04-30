package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RootNode extends Node<RootNodeConfig> {
  ConcurrentHashMap<UUID, Socket> modelCheckingNodes;
  ExecutorService modelCheckingNodesExecutorService;

  public RootNode(RootNodeConfig config) {
    super(config);
    this.modelCheckingNodes = new ConcurrentHashMap<>();
    this.modelCheckingNodesExecutorService = Executors.newCachedThreadPool();
  }

  private Message<VerificationResponse> verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running verify route");

    if (this.modelCheckingNodes.isEmpty()) {
      System.out.println("Could not verify code module: no model checking nodes available");
      return new Message<>(new VerificationResponse(false));
    }

    this.modelCheckingNodes.forEach((uuid, socket) -> {
      try {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.println(new Message<>(message.getPayload()).toJsonString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    UUID nodeUuid = this.modelCheckingNodes.keys().nextElement();
    try {
      PrintWriter printWriter = new PrintWriter(this.modelCheckingNodes.get(nodeUuid).getOutputStream(), true);
      printWriter.println(new Message<>(new StartModelCheckingRequest()).toJsonString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new Message<>(new VerificationResponse(false));
  }

  private Message processSetCodeModuleResponseRoute(Message<SetCodeModuleResponse> message, Socket requestSocket) {
    System.out.println("Running processSetCodeModuleResponse route");
    return null;
  }

  private Message processStartModelCheckingResponseRoute(
    Message<StartModelCheckingResponse> message,
    Socket requestSocket
  ) {
    System.out.println("Running processStartModelCheckingResponseRoute route");
    return null;
  }

  private Message<RegisterModelCheckingNodeResponse> registerModelCheckingNodeRoute(
    Message<RegisterModelCheckingNodeRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running registerModelCheckingNode route");
    UUID uuid = UUID.randomUUID();
    this.modelCheckingNodes.put(uuid, requestSocket);

    this.modelCheckingNodesExecutorService.submit(() -> {
      while (true) {
        try {
          this.run(requestSocket);
        } catch (IOException e) {
          System.out.printf("Model checking node \"%s\" became unavailable: removing node%n", uuid);
          this.modelCheckingNodes.remove(uuid);
          break;
        }
      }
    });

    return new Message<>(new RegisterModelCheckingNodeResponse(uuid));
  }

  public void run(Socket requestSocket) throws IOException {
    String message = MessageUtils.readMessage(requestSocket.getInputStream());

    if (message == null) {
      System.out.println("Received empty message: closing socket");
      requestSocket.close();
    }

    Optional<Message<Object>> messageOptional = this.parseMessage(message);

    if (messageOptional.isEmpty()) {
      System.out.printf("Warning: received invalid message \"%s\"%n", message);
      return;
    }

    this.processMessage(
      messageOptional.get(),
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::verifyRoute,
        RegisterModelCheckingNodeRequest.class, this::registerModelCheckingNodeRoute,
        SetCodeModuleResponse.class, this::processSetCodeModuleResponseRoute,
        StartModelCheckingResponse.class, this::processStartModelCheckingResponseRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    Optional<Integer> portOptional = Main.parsePort(this.config.getPort());

    if (portOptional.isEmpty()) {
      System.out.printf("could not start node: invalid port \"%s\"%n", this.config.getPort());
      return;
    }

    try (
      ServerSocket serverSocket = new ServerSocket(portOptional.get());
      ExecutorService executorService = Executors.newCachedThreadPool()
    ) {
      while (true) {
        Socket requestSocket = serverSocket.accept();
        executorService.submit(() -> {
          try {
            this.run(requestSocket);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        });
      }
    }
  }
}
