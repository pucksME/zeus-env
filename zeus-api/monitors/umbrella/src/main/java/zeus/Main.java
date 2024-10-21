package zeus;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Main {
  public static void main(String[] args) throws IOException {
    System.out.println("On-runtime, temporal, formal verification monitor");
    ServerSocket serverSocket = new ServerSocket(8081);
    Socket socket = serverSocket.accept();
    Request request = new Request(socket.getInputStream());

    if (!request.isValid()) {
      socket.getOutputStream().write(new Response(400, "{\"error\": \"invalid request\"}").toBytes());
      return;
    }

    socket.getOutputStream().write(new Response(200, "{\"reponseData\": \"test\"}").toBytes());
    socket.close();
  }
}
