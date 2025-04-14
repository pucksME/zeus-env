package zeus.shared.message;

public class Message<T> {
  T payload;
  String payloadClassName;

  public Message(T payload) {
    this.payload = payload;
    this.payloadClassName = payload.getClass().getName();
  }

  public T getPayload() {
    return payload;
  }
}
