package zeus.shared.message.utils;

import com.google.gson.JsonObject;

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
}
