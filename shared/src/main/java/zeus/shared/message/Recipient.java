package zeus.shared.message;

import zeus.shared.message.payload.NodeType;

public record Recipient(NodeType nodeType, NodeSelection nodeSelection) {
}
