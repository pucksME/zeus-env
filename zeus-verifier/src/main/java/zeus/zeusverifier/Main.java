package zeus.zeusverifier;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.stream.JsonReader;
import zeus.zeusverifier.config.Config;
import zeus.zeusverifier.config.NodeType;
import zeus.zeusverifier.node.ModelCheckingNode;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.node.RootNode;
import zeus.zeusverifier.utils.ConfigJsonDeserializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  private static Optional<Integer> parsePort(String port) {
    try {
      return Optional.of(Integer.parseInt(port));
    } catch (NumberFormatException numberFormatException) {
      return Optional.empty();
    }
  }

  private static Node getNode(NodeType nodeType) {
    return switch (nodeType) {
      case ROOT_NODE -> new RootNode();
      case MODEL_CHECKING_NODE -> new ModelCheckingNode();
    };
  }

  private static Optional<Config> parseConfig(String path) {
    try {
      JsonReader jsonReader = new JsonReader(new FileReader(path));
      Gson gson = new GsonBuilder().registerTypeAdapter(Config.class, new ConfigJsonDeserializer()).create();
      return Optional.of(gson.fromJson(jsonReader, Config.class));
    } catch (FileNotFoundException e) {
      System.out.printf("Config file \"%s\" did not exist%n", path);
      return Optional.empty();
    } catch (JsonParseException jsonParseException) {
      System.out.printf("Could not parse config file \"%s\"%n", path);
      return Optional.empty();
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("invalid usage: <config>");
      return;
    }

    Optional<Config> configOptional = Main.parseConfig(args[0]);

    if (configOptional.isEmpty()) {
      return;
    }

    Config config = configOptional.get();

    Node node = Main.getNode(config.getNodeType());

    Optional<Integer> portOptional = Main.parsePort(config.getPort());

    if (portOptional.isEmpty()) {
      System.out.printf("could not start node: invalid port \"%s\"%n", config.getPort());
      return;
    }

    try (ServerSocket serverSocket = new ServerSocket(portOptional.get())) {
      ExecutorService executorService = Executors.newCachedThreadPool();
      while (true) {
        Socket socket = serverSocket.accept();
        executorService.submit(() -> {
          try {
            Object result = node.run(socket.getInputStream());
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
            printWriter.println(new Gson().toJson(result));
            socket.close();
          } catch (IOException ioException) {
            throw new RuntimeException(ioException);
          }
        });
      }
    }
  }
}
