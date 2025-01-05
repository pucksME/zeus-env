package zeus;

import com.google.gson.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
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
    } catch (JsonSyntaxException jsonSyntaxException) {
      this.payload = null;
    }
  }

  public boolean isValid() {
    return this.isPost && this.isApplicationJson && this.payload != null;
  }

  public static Map<String, JsonElement> getVariables(String identifier, JsonElement jsonElement) {
    Map<String, JsonElement> variables = new HashMap<>();

    if (jsonElement instanceof JsonObject) {
      for (Map.Entry<String, JsonElement> entry : ((JsonObject) jsonElement).entrySet()) {
        variables = Stream.concat(
          variables.entrySet().stream(),
          Request.getVariables(String.format("%s.%s", identifier, entry.getKey()), entry.getValue()).entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      }
    }

    if (jsonElement instanceof JsonArray) {
      variables.put(identifier, jsonElement);
      for (int i = 0; i < ((JsonArray) jsonElement).size(); i++) {
        variables = Stream.concat(
          variables.entrySet().stream(),
          Request.getVariables(String.format("%s@%s", identifier, i), ((JsonArray) jsonElement).get(i)).entrySet().stream()
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
      }
    }

    if (jsonElement instanceof JsonPrimitive) {
      variables.put(identifier, jsonElement);
    }

    return variables;
  }

  public Map<String, JsonElement> getVariables() {
    Map<String, JsonElement> variables = Stream.concat(
      Request.getVariables("request.url", this.payload.requestUrlParameters).entrySet().stream(),
      Request.getVariables("request.body", this.payload.requestBodyPayload).entrySet().stream()
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> second));

    if (this.payload.responseBodyPayload instanceof JsonNull) {
      return variables;
    }

    return Stream.concat(
      variables.entrySet().stream(),
      Request.getVariables("response.body", this.payload.responseBodyPayload).entrySet().stream()
    ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (first, second) -> second));
  }
}
