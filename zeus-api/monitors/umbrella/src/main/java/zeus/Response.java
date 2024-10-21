package zeus;

import com.google.gson.JsonObject;

public class Response {
  int statusCode;
  JsonObject payload;

  public Response(int statusCode) {
    this.statusCode = statusCode;
    this.payload = new JsonObject();

    if (statusCode == 200) {
      this.payload.addProperty("status", "ok");
    }

    if (statusCode == 400) {
      this.payload.addProperty("status", "invalid request");
    }
  }

  public byte[] toBytes() {
    return String.join("\n", new String[]{
      "HTTP/1.1 200 OK",
      "Content-Type: application/json",
      "",
      this.payload.toString()
    }).getBytes();
  }
}
