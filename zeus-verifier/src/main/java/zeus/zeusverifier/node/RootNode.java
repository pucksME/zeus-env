package zeus.zeusverifier.node;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;

import java.io.InputStream;
import java.util.Optional;

public class RootNode extends Node {
  @Override
  public void run(InputStream inputStream) {
    Optional<ClientCodeModule> clientCodeModuleOptional = this.parseBody(inputStream);
    System.out.println("running root node procedure");
  }
}
