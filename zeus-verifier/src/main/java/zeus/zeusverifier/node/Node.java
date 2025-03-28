package zeus.zeusverifier.node;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class Node {
  protected boolean checkHeader(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    boolean methodChecked = false;
    boolean contentTypeChecked = false;
    while (true) {
      try {
        String line = bufferedReader.readLine();
        if (!methodChecked) {
          if (!line.startsWith("POST")) {
            return false;
          }
          methodChecked = true;
          continue;
        }

        if (!contentTypeChecked) {
          if (line.startsWith("Content-Type")) {
            if (!line.endsWith("application/json")) {
              return false;
            }
            contentTypeChecked = true;
          }
        }

        if (line.isEmpty()) {
          return true;
        }
      } catch (IOException ioException) {
        return false;
      }
    }
  }

  public abstract void run(InputStream inputStream);
}
