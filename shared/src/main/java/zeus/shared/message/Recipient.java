package zeus.shared.message;

import zeus.shared.message.payload.NodeType;

public class Recipient {
  NodeType nodeType;
  NodeSelection nodeSelection;

  public Recipient(NodeType nodeType, NodeSelection nodeSelection) {
    this.nodeType = nodeType;
    this.nodeSelection = nodeSelection;
  }

  public Recipient(NodeType nodeType) {
    this(nodeType, NodeSelection.ANY);
  }

  public NodeType getNodeType() {
    return nodeType;
  }

  public NodeSelection getNodeSelection() {
    return nodeSelection;
  }
}
