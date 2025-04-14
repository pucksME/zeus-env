package zeus.zeusverifier.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import zeus.shared.message.Message;
import zeus.shared.message.utils.MessageJsonDeserializer;
import zeus.shared.message.utils.ObjectJsonDeserializer;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeusverifier.Main;
import zeus.zeusverifier.config.Config;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class Node<T extends Config> {
  T config;

  public Node(T config) {
    this.config = config;
  }

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

  protected void processMessage(
    Message message,
    Socket requestSocket,
    Map<Class<?>, Function<Message, Message>> routes
  ) throws IOException {
    Class<?> payloadClass = message.getPayload().getClass();
    Function<Message, Message> route = routes.get(payloadClass);

    if (route == null) {
      System.out.printf("(Root node) Warning: processed message with unsupported route \"%s\"%n", payloadClass);
      return;
    }

    PrintWriter printWriter = new PrintWriter(requestSocket.getOutputStream(), true);
    printWriter.println(new Gson().toJson(route.apply(message)));
    requestSocket.close();
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
