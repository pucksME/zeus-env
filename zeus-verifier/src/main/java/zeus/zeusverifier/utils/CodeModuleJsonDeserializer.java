package zeus.zeusverifier.utils;

import com.google.gson.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Node;

import java.lang.reflect.Type;

public class CodeModuleJsonDeserializer implements JsonDeserializer<Node> {
  @Override
  public Node deserialize(
    JsonElement jsonElement,
    Type type,
    JsonDeserializationContext jsonDeserializationContext
  ) throws JsonParseException {
    try {
      if (!jsonElement.isJsonObject()) {
        throw new RuntimeException(String.format("Could not deserialize \"%s\": not a json object", jsonElement));
      }

      JsonObject jsonObject = jsonElement.getAsJsonObject();

      if (!jsonObject.has("className")) {
        throw new RuntimeException(String.format(
          "Could not deserialize \"%s\": json object is missing class name",
          jsonElement
        ));
      }

      return jsonDeserializationContext.deserialize(
        jsonElement,
        Class.forName(jsonObject.get("className").getAsString())
      );
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(String.format("Could not deserialize \"%s\": class not found", jsonElement));
    }
  }
}
