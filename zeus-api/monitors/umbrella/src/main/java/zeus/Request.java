package zeus;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Request {
  boolean isPost;
  boolean isApplicationJson;
  RequestPayload payload;

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

    try {
      this.payload = new Gson().fromJson(stringBuilder.toString(), RequestPayload.class);
      if (!this.payload.isValid()) {
        this.payload = null;
      }
    } catch (JsonSyntaxException jsonSyntaxException) {
      this.payload = null;
    }
  }

  public boolean isValid() {
    return this.isPost && this.isApplicationJson && this.payload != null;
  }
}
