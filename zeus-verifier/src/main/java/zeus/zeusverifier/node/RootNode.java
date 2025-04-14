package zeus.zeusverifier.node;

import zeus.shared.message.Message;
import zeus.shared.message.payload.RegisterModelCheckingNode;
import zeus.shared.message.payload.RegisteredModelCheckingNode;
import zeus.shared.message.payload.VerificationResult;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;

public class RootNode extends Node<RootNodeConfig> {
  public RootNode(RootNodeConfig config) {
    super(config);
  }

  private Message<VerificationResult> verifyRoute(Message<ClientCodeModule> message) {
    System.out.println("(Root node) Running verify route");
    return new Message<>(new VerificationResult(false));
  }

  private Message<RegisteredModelCheckingNode> registerModelCheckingNodeRoute(Message<RegisterModelCheckingNode> message) {
    System.out.println("(Root node) Running registerModelCheckingNode route");
    return new Message<>(new RegisteredModelCheckingNode());
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
        RegisterModelCheckingNode.class, this::registerModelCheckingNodeRoute
      )
    );
  }
}
