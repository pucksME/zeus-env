package zeus.shared.message.utils;

import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class MessageUtils {
  public static Class<?> getClass(JsonObject jsonObject, String propertyName) {
    try {
      if (!jsonObject.has(propertyName)) {
        throw new RuntimeException(String.format(
          "Could not deserialize \"%s\": json object is missing payload class name",
          jsonObject
        ));
      }

      return Class.forName(jsonObject.get(propertyName).getAsString());
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(String.format("Could not deserialize \"%s\": class not found", jsonObject));
    }
  }

  public static String readMessage(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder stringBuilder = new StringBuilder();

    try {
      while (bufferedReader.ready()) {
        String line = bufferedReader.readLine();
        stringBuilder.append(line);
      }
    } catch (IOException ioException) {
      return "";
    }

    return stringBuilder.toString();
  }
}
