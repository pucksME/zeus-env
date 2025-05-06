package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.payload.modelchecking.*;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModelCheckingNode extends Node<ModelCheckingNodeConfig> {
  UUID uuid;
  ClientCodeModule codeModule;

  public ModelCheckingNode(ModelCheckingNodeConfig config) {
    super(config);
  }

  @Override
  public void start() throws IOException {
    String rootNodePort = this.getConfig().getRootNode().getPort();
    Optional<Integer> rootNodePortOptional = Main.parsePort(rootNodePort);

    if (rootNodePortOptional.isEmpty()) {
      throw new RuntimeException(String.format(
        "Could not register model checking node: invalid root node port \"%s\"",
        rootNodePort
      ));
    }

    try (Socket socket = new Socket(this.getConfig().getRootNode().getHost(), rootNodePortOptional.get())) {
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println(new Message<>(new RegisterModelCheckingNodeRequest()).toJsonString());
      Optional<Message<RegisterModelCheckingNodeResponse>> messageOptional = this.parseMessage(
        MessageUtils.readMessage(socket.getInputStream())
      );

      if (messageOptional.isEmpty()) {
        System.out.println("Could not register model checking node: invalid response message");
        socket.close();
        return;
      }

      this.uuid = messageOptional.get().getPayload().uuid();

      System.out.printf(
        "Successfully registered model checking node \"%s\"%n",
        this.uuid
      );

      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      while (true) {
        try (ExecutorService executorService = Executors.newCachedThreadPool()) {
          String message = bufferedReader.readLine();

          if (message == null) {
            System.out.println("Received empty message: stopping node");
            break;
          }

          executorService.submit(() -> {
            try {
              NodeAction nodeAction = this.run(message, socket);

              if (nodeAction == NodeAction.NONE) {
                return;
              }

              if (nodeAction == NodeAction.TERMINATE) {
                this.terminate(socket, executorService);
              }
            } catch (IOException ioException) {
              System.out.println("Root node became unavailable: stopping node");
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
  }

  private RouteResult setCodeModuleRoute(Message<ClientCodeModule> message, Socket socket) {
    System.out.println("Running setCodeModuleRoute route");
    this.codeModule = message.getPayload();
    return new RouteResult(new Message<>(new SetCodeModuleResponse()));
  }

  private RouteResult startModelCheckingRoute(
    Message<StartModelCheckingRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running startModelChecking route");

    CodeModuleIterator codeModuleIterator = new CodeModuleIterator(this.codeModule);
    if (codeModuleIterator.calibrate(message.getPayload().path())) {
      return new RouteResult(new Message<>(new CalibrationFailed(message.getPayload().path())), NodeAction.TERMINATE);
    }

    return new RouteResult(new Message<>(new StartModelCheckingResponse()));
  }

  public NodeAction run(String message, Socket socket) throws IOException {
    Optional<Message<Object>> messageOptional = this.parseMessage(message);

    if (messageOptional.isEmpty()) {
      System.out.printf("Warning: received invalid message \"%s\"%n", message);
      return NodeAction.TERMINATE;
    }

    return this.processMessage(
      messageOptional.get(),
      socket,
      Map.of(
        ClientCodeModule.class, this::setCodeModuleRoute,
        StartModelCheckingRequest.class, this::startModelCheckingRoute
      )
    );
  }
}
