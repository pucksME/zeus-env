package zeus.zeusverifier.config.modelcheckingnode;

import com.google.gson.annotations.SerializedName;
import zeus.zeusverifier.config.Config;

public class ModelCheckingNodeConfig extends Config {
  @SerializedName("root-node")
  RootNode rootNode;

  public ModelCheckingNodeConfig(String type, RootNode rootNode) {
    super(type);
  }

  public RootNode getRootNode() {
    return rootNode;
  }
}
