package zeus.zeusverifier.node.storage;

import zeus.shared.message.Message;
import zeus.zeusverifier.config.storagenode.StorageNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class StorageNode extends Node<StorageNodeConfig> {
  public StorageNode(StorageNodeConfig config) {
    super(config);
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of()
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
