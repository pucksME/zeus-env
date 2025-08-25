package zeus.shared.message;

import zeus.shared.message.payload.NodeType;

import java.util.Optional;
import java.util.UUID;

public class Recipient {
  private final NodeType nodeType;
  private final NodeSelection nodeSelection;
  private UUID nodeUuid;

  public Recipient(NodeType nodeType, NodeSelection nodeSelection) {
    this.nodeType = nodeType;
    this.nodeSelection = nodeSelection;
  }

  public Recipient(NodeType nodeType) {
    this(nodeType, NodeSelection.ANY);
  }

  public Recipient(NodeType nodeType, UUID nodeUuid) {
    this(nodeType, NodeSelection.ALL);
    this.nodeUuid = nodeUuid;
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public NodeSelection getNodeSelection() {
    return nodeSelection;
  }

  public Optional<UUID> getNodeUuid() {
    return Optional.ofNullable(nodeUuid);
  }
}
