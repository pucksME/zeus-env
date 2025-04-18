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
import zeus.zeusverifier.config.Config;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

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
    Map<Class<?>, BiFunction<Message, Socket, Message>> routes
  ) throws IOException {
    Class<?> payloadClass = message.getPayload().getClass();
    BiFunction<Message, Socket, Message> route = routes.get(payloadClass);

    if (route == null) {
      System.out.printf("(Root node) Warning: processed message with unsupported route \"%s\"%n", payloadClass);
      return;
    }

    Message responseMessage = route.apply(message, requestSocket);
    if (responseMessage == null) {
      return;
    }

    PrintWriter printWriter = new PrintWriter(requestSocket.getOutputStream(), true);
    printWriter.println(responseMessage.toJsonString());
  }

  public abstract void start() throws IOException;
}
