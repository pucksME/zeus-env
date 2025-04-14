package zeus.zeusverifier.node;

import com.google.gson.Gson;
import zeus.shared.message.Message;
import zeus.shared.message.payload.RegisterModelCheckingNode;
import zeus.shared.message.payload.RegisteredModelCheckingNode;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingNodeConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class ModelCheckingNode extends Node<ModelCheckingNodeConfig> {
  public ModelCheckingNode(ModelCheckingNodeConfig config) {
    super(config);
  }

  @Override
  public void start() throws IOException {
    String rootNodePort = this.config.getRootNode().getPort();
    Optional<Integer> rootNodePortOptional = Main.parsePort(rootNodePort);

    if (rootNodePortOptional.isEmpty()) {
      throw new RuntimeException(String.format(
        "Could not register model checking node: invalid root node port \"%s\"",
        rootNodePort
      ));
    }

    try (Socket socket = new Socket(this.config.getRootNode().getHost(), rootNodePortOptional.get())) {
      PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
      printWriter.println(new Gson().toJson(new Message<>(new RegisterModelCheckingNode())));
      Optional<Message<RegisteredModelCheckingNode>> messageOptional = this.parseMessage(MessageUtils.readMessage(socket.getInputStream()));

      if (messageOptional.isEmpty()) {
        System.out.println("(Model checking node) Could not register model checking node: invalid response message");
        socket.close();
        return;
      }

      System.out.println("(Model checking node) Successfully registered model checking node");
    }

    super.start();
  }

  @Override
  public void run(Socket requestSocket) throws IOException {
    System.out.println("running model checking node procedure");
  }
}
