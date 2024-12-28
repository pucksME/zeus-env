package zeus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class RequestPayload {
  JsonObject requestUrlParameters;
  JsonObject requestBodyPayload;
  String context;
  String server;
  String route;
  JsonElement responseBodyPayload;

  public RequestPayload(
    JsonObject requestUrlParameters,
    JsonObject requestBodyPayload,
    String context,
    String server,
    String route,
    JsonObject responseBodyPayload
  ) {
    this.requestUrlParameters = requestUrlParameters;
    this.requestBodyPayload = requestBodyPayload;
    this.context = context;
    this.server = server;
    this.route = route;
    this.responseBodyPayload = responseBodyPayload;
  }

  public boolean isValid() {
    return this.requestUrlParameters != null &&
      this.requestBodyPayload != null &&
      this.context != null &&
      this.server != null &&
      this.route != null;
  }
}
