package zeus.shared.message;

import com.google.gson.Gson;

import java.util.Optional;

public class Message<T> {
  T payload;
  String payloadClassName;
  private Recipient recipient;

  public Message(T payload) {
    this.payload = payload;
    this.payloadClassName = payload.getClass().getName();
  }

  public Message(T payload, Recipient recipient) {
    this(payload);
    this.recipient = recipient;
  }

  public T getPayload() {
    return payload;
  }

  public String toJsonString() {
    return new Gson().toJson(this);
  }

  public void removeRecipient() {
    this.recipient = null;
  }

  public Optional<Recipient> getRecipient() {
    return Optional.ofNullable(recipient);
  }
}
