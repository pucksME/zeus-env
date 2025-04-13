package zeus.zeusverifier.node;

import com.google.gson.Gson;
import zeus.zeuscompiler.rain.compiler.VerificationResult;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Optional;

public class RootNode extends Node {
  public RootNode(RootNodeConfig config) {
    super(config);
  }

  @Override
  public void run(Socket requestSocket) throws IOException {
    Optional<ClientCodeModule> clientCodeModuleOptional = this.parseBody(requestSocket.getInputStream());

    System.out.println("running root node procedure");

    PrintWriter printWriter = new PrintWriter(requestSocket.getOutputStream(), true);
    printWriter.println(new Gson().toJson(new VerificationResult(false)));
    requestSocket.close();
  }
}
