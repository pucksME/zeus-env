package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.*;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

public class RootNode extends Node<RootNodeConfig> {
  ConcurrentHashMap<UUID, Socket> modelCheckingNodes;
  ExecutorService modelCheckingNodesExecutorService;

  public RootNode(RootNodeConfig config) {
    super(config);
    this.modelCheckingNodes = new ConcurrentHashMap<>();
    this.modelCheckingNodesExecutorService = Executors.newCachedThreadPool();
  }

  private RouteResult verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running verify route");

    if (this.modelCheckingNodes.isEmpty()) {
      System.out.println("Could not verify code module: no model checking nodes available");
      return new RouteResult(new Message<>(new VerificationResponse(false)));
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
      printWriter.println(new Message<>(new StartModelCheckingRequest(new Path(
        new ArrayList<>(),
        new HashSet<>()
      ))).toJsonString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new RouteResult(new Message<>(new VerificationResponse(false)));
  }

  private RouteResult processSetCodeModuleResponseRoute(Message<SetCodeModuleResponse> message, Socket requestSocket) {
    System.out.println("Running processSetCodeModuleResponse route");
    return new RouteResult();
  }

  private RouteResult processStartModelCheckingResponseRoute(
    Message<StartModelCheckingResponse> message,
    Socket requestSocket
  ) {
    System.out.println("Running processStartModelCheckingResponseRoute route");
    return new RouteResult();
  }

  private RouteResult processCalibrationFailedRoute(Message<CalibrationFailed> message, Socket requestSocket) {
    System.out.println("Model checking node \"%s\" could not calibrate path:");
    System.out.println(message.getPayload().path());
    return new RouteResult(NodeAction.TERMINATE);
  }

  private RouteResult registerModelCheckingNodeRoute(
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

    return new RouteResult(new Message<>(new RegisterModelCheckingNodeResponse(uuid)));
  }

  public NodeAction run(Socket requestSocket) throws IOException {
    String message = MessageUtils.readMessage(requestSocket.getInputStream());

    if (message == null) {
      System.out.println("Received empty message: closing socket");
      requestSocket.close();
    }

    Optional<Message<Object>> messageOptional = this.parseMessage(message);

    if (messageOptional.isEmpty()) {
      System.out.printf("Warning: received invalid message \"%s\"%n", message);
      return NodeAction.TERMINATE;
    }

    return this.processMessage(
      messageOptional.get(),
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::verifyRoute,
        RegisterModelCheckingNodeRequest.class, this::registerModelCheckingNodeRoute,
        SetCodeModuleResponse.class, this::processSetCodeModuleResponseRoute,
        StartModelCheckingResponse.class, this::processStartModelCheckingResponseRoute,
        CalibrationFailed.class, this::processCalibrationFailedRoute
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
            NodeAction nodeAction = this.run(requestSocket);

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
}
