package zeus.zeuscompiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Analyzer<T> {
  CompilerPhase compilerPhase;
  List<CompilerError> compilerErrors;

  public Analyzer(CompilerPhase compilerPhase) {
    this.compilerPhase = compilerPhase;
    this.compilerErrors = new ArrayList<>();
  }

  public abstract CommonTokenStream runLexer(CharStream code);

  public abstract ParseTree runParser(CommonTokenStream tokens);

  public abstract Optional<T> analyze(CharStream code);

  public CompilerPhase getCompilerPhase() {
    return compilerPhase;
  }
}
