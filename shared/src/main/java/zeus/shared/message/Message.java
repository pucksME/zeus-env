package zeus.shared.message;

import com.google.gson.Gson;

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

  public String toJsonString() {
    return new Gson().toJson(this);
  }
}
