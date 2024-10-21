package zeus;

public class Response {
  int statusCode;
  String payload;

  public Response(int statusCode, String payload) {
    this.statusCode = statusCode;
    this.payload = payload;
  }

  public byte[] toBytes() {
    return String.join("\n", new String[]{
      "HTTP/1.1 200 OK",
      "Content-Type: application/json",
      "",
      this.payload
    }).getBytes();
  }
}
