package zeus.zeusverifier.node;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeusverifier.utils.CodeModuleJsonDeserializer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;

public abstract class Node<T> {
  private BufferedReader requestBufferedReader;
  protected boolean checkHeader(InputStream inputStream) {
    this.requestBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    boolean methodChecked = false;
    boolean contentTypeChecked = false;
    while (true) {
      try {
        String line = this.requestBufferedReader.readLine();
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

  protected Optional<ClientCodeModule> parseBody(InputStream inputStream) {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder stringBuilder = new StringBuilder();

    try {
      while (bufferedReader.ready()) {
        stringBuilder.append((char) bufferedReader.read());
      }
    } catch (IOException ioException) {
      return Optional.empty();
    }

    String codeModuleJson = stringBuilder.toString();
    Gson gson = new GsonBuilder()
      .registerTypeAdapter(Type.class, new CodeModuleJsonDeserializer())
      .registerTypeAdapter(BodyComponent.class, new CodeModuleJsonDeserializer())
      .registerTypeAdapter(Expression.class, new CodeModuleJsonDeserializer())
      .create();

    try {
      ClientCodeModule codeModule = gson.fromJson(stringBuilder.toString(), ClientCodeModule.class);
      return Optional.of(codeModule);
    } catch (JsonParseException jsonParseException) {
      System.out.printf("Could not deserialize \"%s\": parsing failed%n", codeModuleJson);
      return Optional.empty();
    }
  }

  public abstract T run(InputStream inputStream);
}
