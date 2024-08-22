package zeus.zeuscompiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class Analyzer<T> {
  CompilerPhase compilerPhase;
  ClientSymbolTable symbolTable;
  List<CompilerError> compilerErrors;

  public Analyzer(CompilerPhase compilerPhase) {
    this.compilerPhase = compilerPhase;
    this.symbolTable = new ClientSymbolTable();
    this.compilerErrors = new ArrayList<>();
  }

  public abstract CommonTokenStream runLexer(CharStream code);

  public abstract ParseTree runParser(CommonTokenStream tokens);

  public abstract Optional<T> analyze(CharStream code);

  public void reset() {
    this.compilerErrors.clear();
    this.symbolTable = new ClientSymbolTable();
  }

  public boolean hasErrors() {
    return this.compilerErrors.size() != 0;
  }

  public CompilerPhase getCompilerPhase() {
    return compilerPhase;
  }

  public ClientSymbolTable getSymbolTable() {
    return symbolTable;
  }

  public List<CompilerError> getErrors() {
    return compilerErrors;
  }
}
