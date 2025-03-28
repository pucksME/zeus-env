package zeus.zeusverifier.node;

import java.io.InputStream;

public class RootNode extends Node {
  @Override
  public void run(InputStream inputStream) {
    if (!this.checkHeader(inputStream)) {
      System.out.println("warning: root node received invalid request header");
      return;
    }

    System.out.println("running root node procedure");
  }
}
