package zeus.shared.message.utils;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ObjectJsonDeserializer<T> implements JsonDeserializer<T> {
  @Override
  public T deserialize(
    JsonElement jsonElement,
    Type type,
    JsonDeserializationContext jsonDeserializationContext
  ) throws JsonParseException {

    if (!jsonElement.isJsonObject()) {
      throw new RuntimeException(String.format("Could not deserialize \"%s\": not a json object", jsonElement));
    }

    return jsonDeserializationContext.deserialize(
      jsonElement,
      MessageUtils.getClass(jsonElement.getAsJsonObject(), "className")
    );
  }
}
