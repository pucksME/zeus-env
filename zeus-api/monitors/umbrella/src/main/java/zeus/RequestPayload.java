package zeus;

import com.google.gson.JsonObject;

public class RequestPayload {
  JsonObject urlParameters;
  JsonObject bodyPayload;
  String context;
  String server;
  String route;

  public RequestPayload(JsonObject urlParameters, JsonObject bodyPayload, String context, String server, String route) {
    this.urlParameters = urlParameters;
    this.bodyPayload = bodyPayload;
    this.context = context;
    this.server = server;
    this.route = route;
  }

  public boolean isValid() {
    return this.urlParameters != null &&
      this.bodyPayload != null &&
      this.context != null &&
      this.server != null &&
      this.route != null;
  }
}
