package zeus.zeusverifier.routing;

import zeus.shared.message.Message;

import java.util.Optional;

public class RouteResult {
  Message<?> responseMessage;
  NodeAction nodeAction;

  public RouteResult(Message<?> responseMessage, NodeAction nodeAction) {
    this.responseMessage = responseMessage;
    this.nodeAction = nodeAction;
  }

  public RouteResult(Message<?> responseMessage) {
    this.responseMessage = responseMessage;
    this.nodeAction = NodeAction.NONE;
  }

  public RouteResult(NodeAction nodeAction) {
    this.responseMessage = null;
    this.nodeAction = nodeAction;
  }

  public RouteResult() {
    this.responseMessage = null;
    this.nodeAction = NodeAction.NONE;
  }

  public Optional<Message<?>> getResponseMessage() {
    return Optional.ofNullable(responseMessage);
  }

  public NodeAction getNodeAction() {
    return nodeAction;
  }
}
