package zeus;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Request {
  boolean isPost;
  boolean isApplicationJson;
  String payload;

  public Request(InputStream inputStream) throws IOException {
    this.isPost = false;
    this.isApplicationJson = false;

    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    boolean isFirstLine = true;
    while (true) {
      String line = bufferedReader.readLine();
      if (line.isEmpty()) {
        break;
      }

      if (isFirstLine) {
        isPost = line.startsWith("POST");
        isFirstLine = false;
      }

      if (line.startsWith("Content-Type")) {
        this.isApplicationJson = line.endsWith("application/json");
      }
    }

    StringBuilder stringBuilder = new StringBuilder();

    while (bufferedReader.ready()) {
      char symbol = (char) bufferedReader.read();
      stringBuilder.append(symbol);
    }

    this.payload = stringBuilder.toString();
  }

  public boolean isValid() {
    return this.isPost && this.isApplicationJson;
  }
}
