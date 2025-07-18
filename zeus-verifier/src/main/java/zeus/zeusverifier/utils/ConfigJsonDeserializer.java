package zeus.zeusverifier.utils;

import com.google.gson.*;
import zeus.zeusverifier.config.Config;
import zeus.zeusverifier.config.abstractionnode.AbstractionGatewayNodeConfig;
import zeus.zeusverifier.config.abstractionnode.AbstractionNodeConfig;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisGatewayNodeConfig;
import zeus.zeusverifier.config.counterexampleanalysisnode.CounterExampleAnalysisNodeConfig;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingNodeConfig;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingGatewayNodeConfig;
import zeus.zeusverifier.config.rootnode.GatewayNodeConfig;

import java.lang.reflect.Type;

public class ConfigJsonDeserializer implements JsonDeserializer<Config> {
  @Override
  public Config deserialize(
    JsonElement jsonElement,
    Type type,
    JsonDeserializationContext jsonDeserializationContext
  ) throws JsonParseException {
    if (!jsonElement.isJsonObject()) {
      throw new RuntimeException(String.format(
        "Could not deserialize config: invalid json element \"%s\"",
        jsonElement
      ));
    }

    JsonObject jsonObject = jsonElement.getAsJsonObject();

    if (!jsonObject.has("type")) {
      throw new RuntimeException(String.format(
        "Could not deserialize config: mising type property in \"%s\"",
        jsonElement
      ));
    }

    String nodeType = jsonObject.get("type").getAsString();

    return switch (nodeType) {
      case "root-node" -> jsonDeserializationContext.deserialize(jsonElement, GatewayNodeConfig.class);
      case "model-checking-gateway-node" -> jsonDeserializationContext.deserialize(jsonElement, ModelCheckingGatewayNodeConfig.class);
      case "model-checking-node" -> jsonDeserializationContext.deserialize(jsonElement, ModelCheckingNodeConfig.class);
      case "abstraction-gateway-node" -> jsonDeserializationContext.deserialize(jsonElement, AbstractionGatewayNodeConfig.class);
      case "abstraction-node" -> jsonDeserializationContext.deserialize(jsonElement, AbstractionNodeConfig.class);
      case "counterexample-analysis-gateway-node" -> jsonDeserializationContext.deserialize(jsonElement, CounterExampleAnalysisGatewayNodeConfig.class);
      case "counterexample-analysis-node" -> jsonDeserializationContext.deserialize(jsonElement, CounterExampleAnalysisNodeConfig.class);
      default -> throw new RuntimeException(String.format(
        "Could not deserialize config: unsupported type \"%s\"",
        nodeType
      ));
    };
  }
}
