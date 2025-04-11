package zeus.zeusverifier;

import com.google.gson.Gson;
import zeus.zeusverifier.node.ModelCheckingNode;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.node.RootNode;

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

  private static Optional<Node> parseType(String type) {
    return Optional.ofNullable(switch (type) {
      case "root" -> new RootNode();
      case "model-checking" -> new ModelCheckingNode();
      default -> null;
    });
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("invalid usage: <type> <port>");
      return;
    }

    Optional<Node> nodeOptional = Main.parseType(args[0]);
    if (nodeOptional.isEmpty()) {
      System.out.printf("could not start node: invalid type \"%s\"%n", args[0]);
      return;
    }

    Optional<Integer> portOptional = Main.parsePort(args[1]);
    if (portOptional.isEmpty()) {
      System.out.printf("could not start node: invalid port \"%s\"%n", args[1]);
      return;
    }

    try (ServerSocket serverSocket = new ServerSocket(portOptional.get())) {
      ExecutorService executorService = Executors.newCachedThreadPool();
      while (true) {
        Socket socket = serverSocket.accept();
        executorService.submit(() -> {
          try {
            Object result = nodeOptional.get().run(socket.getInputStream());
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
