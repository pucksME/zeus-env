package zeus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {
  public static void main(String[] args) throws IOException {
    System.out.println("On-runtime, temporal, formal verification monitor");

    ServerSocket serverSocket = new ServerSocket(8081);
    ExecutorService executorService = Executors.newCachedThreadPool();

    while (true) {
      Socket socket = serverSocket.accept();
      executorService.submit(() -> {
        try {
          Request request = new Request(socket.getInputStream());

          if (!request.isValid()) {
            socket.getOutputStream().write(new Response(
              400,
              "{\"error\": \"invalid request\"}"
            ).toBytes());
            socket.close();
            return;
          }

          socket.getOutputStream().write(new Response(
            200,
            "{\"reponseData\": \"test\"}"
          ).toBytes());
          socket.close();
        } catch (IOException ioException) {
          ioException.printStackTrace();
        } finally {
          try {
            socket.close();
          } catch (IOException ioException) {
            ioException.printStackTrace();
          }
        }
      });
    }
  }
}
