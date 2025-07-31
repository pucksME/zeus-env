package zeus.zeuscompiler.thunder.compiler.syntaxtree;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;

public abstract class Node {
  int line;
  int linePosition;
  final String className;

  public Node(int line, int linePosition) {
    this.line = line;

    CompilerErrorService compilerErrorService = ServiceProvider.provide(CompilerErrorService.class);

    if (compilerErrorService != null) {
      this.line += compilerErrorService.getLineOffset();
    }

    this.linePosition = linePosition;
    this.className = this.getClass().getName();
  }

  public int getLine() {
    return line;
  }

  public int getLinePosition() {
    return linePosition;
  }
}
