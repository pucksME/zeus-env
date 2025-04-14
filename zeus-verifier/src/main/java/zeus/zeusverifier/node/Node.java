package zeus.zeusverifier.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import zeus.shared.message.Message;
import zeus.shared.message.utils.MessageJsonDeserializer;
import zeus.shared.message.utils.ObjectJsonDeserializer;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Node {
  Config config;

  public Node(Config config) {
    this.config = config;
  }

  protected <T> Optional<Message<T>> parseMessage(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder stringBuilder = new StringBuilder();

    try {
      while (bufferedReader.ready()) {
        stringBuilder.append((char) bufferedReader.read());
      }
    } catch (IOException ioException) {
      return Optional.empty();
    }

    String codeModuleJson = stringBuilder.toString();
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Message.class, new MessageJsonDeserializer<Message<ClientCodeModule>>())
      .registerTypeAdapter(Type.class, new ObjectJsonDeserializer<Type>())
      .registerTypeAdapter(BodyComponent.class, new ObjectJsonDeserializer<BodyComponent>())
      .registerTypeAdapter(Expression.class, new ObjectJsonDeserializer<Expression>())
      .create();

    try {
      return Optional.of(gson.fromJson(stringBuilder.toString(), Message.class));
    } catch (JsonParseException jsonParseException) {
      System.out.printf("Could not deserialize \"%s\": parsing failed%n", codeModuleJson);
      return Optional.empty();
    }
  }

  public abstract void run(Socket requestSocket) throws IOException;

  public void start() throws IOException {
    Optional<Integer> portOptional = Main.parsePort(this.config.getPort());

    if (portOptional.isEmpty()) {
      System.out.printf("could not start node: invalid port \"%s\"%n", this.config.getPort());
      return;
    }

    while (true) {
      try (
        ServerSocket serverSocket = new ServerSocket(portOptional.get());
        ExecutorService executorService = Executors.newCachedThreadPool()
      ) {
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
