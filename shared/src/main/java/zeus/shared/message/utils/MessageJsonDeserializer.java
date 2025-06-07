package zeus.shared.message.utils;

import com.google.gson.*;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;

import java.lang.reflect.Type;

public class MessageJsonDeserializer<T> implements JsonDeserializer<Message<T>> {
  @Override
  public Message<T> deserialize(
    JsonElement jsonElement,
    Type type,
    JsonDeserializationContext jsonDeserializationContext
  ) throws JsonParseException {

    if (!jsonElement.isJsonObject()) {
      throw new RuntimeException(String.format("Could not deserialize \"%s\": not a json object", jsonElement));
    }

    JsonObject jsonObject = jsonElement.getAsJsonObject();

    if (!jsonObject.has("payload")) {
      throw new RuntimeException(String.format("Could not deserialize \"%s\": no payload", jsonElement));
    }

    return new Message<T>(
      jsonDeserializationContext.deserialize(
        jsonObject.get("payload"),
        MessageUtils.getClass(jsonObject, "payloadClassName")
      ),
      jsonDeserializationContext.deserialize(jsonObject.get("recipient"), Recipient.class)
    );
  }
}
