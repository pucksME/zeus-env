package zeus;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
      this.getVariables();
    } catch (JsonSyntaxException jsonSyntaxException) {
      this.payload = null;
    }
  }

  public boolean isValid() {
    return this.isPost && this.isApplicationJson && this.payload != null;
  }

  private Map<String, String> getVariables(String identifier, Set<Map.Entry<String, JsonElement>> entrySet) {
    Map<String, String> variables = new HashMap<>();

    for (Map.Entry<String, JsonElement> entry : entrySet) {
      if (entry.getValue() instanceof JsonObject) {
        variables = Stream.concat(
          variables.entrySet().stream(),
          this.getVariables(String.format("%s.%s", identifier, entry.getKey()), ((JsonObject) entry.getValue()).entrySet()).entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      }

      if (entry.getValue() instanceof JsonPrimitive) {
        variables.put(String.format("%s.%s", identifier, entry.getKey()), entry.getValue().getAsString());
      }
    }

    return variables;
  }

  public Map<String, String> getVariables() {
    return Stream.concat(
      this.getVariables("request.url", this.payload.urlParameters.entrySet()).entrySet().stream(),
      this.getVariables("request.body", this.payload.bodyPayload.entrySet()).entrySet().stream()
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> second));
  }
}
