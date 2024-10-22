package zeus;

import com.google.gson.JsonObject;

public class RequestPayload {
  JsonObject urlParameters;
  JsonObject bodyPayload;
  String ip;

  public RequestPayload(JsonObject urlParameters, JsonObject bodyPayload, String ip) {
    this.urlParameters = urlParameters;
    this.bodyPayload = bodyPayload;
    this.ip = ip;
  }

  public boolean valid() {
    return this.urlParameters != null && this.bodyPayload != null && this.ip != null;
  }
}
