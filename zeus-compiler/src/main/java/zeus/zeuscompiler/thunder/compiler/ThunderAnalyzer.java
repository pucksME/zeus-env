package zeus.zeuscompiler.thunder.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.Analyzer;
import zeus.zeuscompiler.grammars.ThunderLexer;
import zeus.zeuscompiler.grammars.ThunderParser;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderLexerErrorListener;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderParserErrorListener;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class ThunderAnalyzer extends Analyzer<CodeModules> {
  ThunderAnalyzerMode thunderAnalyzerMode;
  public ThunderAnalyzer(CompilerPhase compilerPhase, ThunderAnalyzerMode thunderAnalyzerMode) {
    super(compilerPhase);
    this.thunderAnalyzerMode = thunderAnalyzerMode;
  }

  @Override
  public CommonTokenStream runLexer(CharStream code) {
    ThunderLexer thunderLexer = new ThunderLexer(code);
    thunderLexer.removeErrorListeners();
    thunderLexer.addErrorListener(new ThunderLexerErrorListener());
    CommonTokenStream tokens = new CommonTokenStream(thunderLexer);
    tokens.fill();
    return tokens;
  }

  @Override
  public ParseTree runParser(CommonTokenStream tokens) {
    ThunderParser thunderParser = new ThunderParser(tokens);
    thunderParser.removeErrorListeners();
    thunderParser.addErrorListener(new ThunderParserErrorListener());
    return thunderParser.codeModules();
  }

  private CodeModules runTypeChecker(ParseTree parseTree) {
    ThunderTypeChecker thunderTypeChecker = new ThunderTypeChecker(parseTree, this.thunderAnalyzerMode);
    return thunderTypeChecker.checkTypes();
  }

  public Optional<CodeModules> analyze(String thunderCode) {
    return this.analyze(CharStreams.fromString(thunderCode));
  }

  @Override
  public Optional<CodeModules> analyze(CharStream thunderCode) {
    CommonTokenStream tokens;

    switch (this.getCompilerPhase()) {
      case LEXER:
        runLexer(thunderCode);
        return Optional.empty();
      case PARSER:
        tokens = runLexer(thunderCode);
        runParser(tokens);
        return Optional.empty();
      case TYPE_CHECKER:
        tokens = runLexer(thunderCode);
        ParseTree parseTree = runParser(tokens);
        if (ServiceProvider.provide(CompilerErrorService.class).hasErrors()) {
          return Optional.empty();
        }
        return Optional.of(runTypeChecker(parseTree));
    }

    return Optional.empty();
  }
}
