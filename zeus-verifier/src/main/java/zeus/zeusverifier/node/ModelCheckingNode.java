package zeus.zeusverifier.node;

import java.io.InputStream;

public class ModelCheckingNode extends Node {
  @Override
  public void run(InputStream inputStream) {
    if (!this.checkHeader(inputStream)) {
      System.out.println("warning: model checking node received invalid request header");
      return;
    }

    System.out.println("running model checking node procedure");
  }
}
