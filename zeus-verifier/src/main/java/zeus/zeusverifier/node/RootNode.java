package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.*;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class RootNode extends Node<RootNodeConfig> {
  ConcurrentHashMap<UUID, Socket> modelCheckingNodes;

  public RootNode(RootNodeConfig config) {
    super(config);
    this.modelCheckingNodes = new ConcurrentHashMap<>();
  }

  private Message<VerificationResponse> verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("(Root node) Running verify route");

    if (this.modelCheckingNodes.isEmpty()) {
      System.out.println("(Root node) Could not verify code module: no model checking nodes available");
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

  private Message processSetCodeModuleResponse(Message<SetCodeModuleResponse> message, Socket requestSocket) {
    System.out.println("(Root node) Running processSetCodeModuleResponse route");
    return null;
  }

  private Message<RegisterModelCheckingNodeResponse> registerModelCheckingNodeRoute(
    Message<RegisterModelCheckingNodeRequest> message,
    Socket requestSocket
  ) {
    System.out.println("(Root node) Running registerModelCheckingNode route");
    UUID uuid = UUID.randomUUID();
    this.modelCheckingNodes.put(uuid, requestSocket);
    return new Message<>(new RegisterModelCheckingNodeResponse(uuid));
  }

  @Override
  public void run(Socket requestSocket) throws IOException {
    System.out.println("(Root node) Received message");
    String message = MessageUtils.readMessage(requestSocket.getInputStream());
    Optional<Message<Object>> messageOptional = this.parseMessage(message);

    if (messageOptional.isEmpty()) {
      System.out.printf("(Root node) Warning: received invalid message \"%s\"%n", message);
      return;
    }

    this.processMessage(
      messageOptional.get(),
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::verifyRoute,
        RegisterModelCheckingNodeRequest.class, this::registerModelCheckingNodeRoute,
        SetCodeModuleResponse.class, this::processSetCodeModuleResponse
      )
    );
  }
}
