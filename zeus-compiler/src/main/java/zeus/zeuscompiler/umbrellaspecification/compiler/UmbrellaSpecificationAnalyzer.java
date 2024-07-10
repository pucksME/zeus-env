package zeus.zeuscompiler.umbrellaspecification.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.Analyzer;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationLexer;
import zeus.zeuscompiler.grammars.UmbrellaSpecificationParser;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderLexerErrorListener;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderParserErrorListener;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;

import java.util.Optional;

public class UmbrellaSpecificationAnalyzer extends Analyzer<UmbrellaSpecifications> {
  public UmbrellaSpecificationAnalyzer(CompilerPhase compilerPhase) {
    super(compilerPhase);
  }

  @Override
  public CommonTokenStream runLexer(CharStream code) {
    UmbrellaSpecificationLexer umbrellaSpecificationLexer = new UmbrellaSpecificationLexer(code);
    umbrellaSpecificationLexer.removeErrorListeners();
    umbrellaSpecificationLexer.addErrorListener(new ThunderLexerErrorListener(this.getErrors()));
    CommonTokenStream tokens = new CommonTokenStream(umbrellaSpecificationLexer);
    tokens.fill();
    return tokens;
  }

  @Override
  public ParseTree runParser(CommonTokenStream tokens) {
    UmbrellaSpecificationParser umbrellaSpecificationParser = new UmbrellaSpecificationParser(tokens);
    umbrellaSpecificationParser.removeErrorListeners();
    umbrellaSpecificationParser.addErrorListener(new ThunderParserErrorListener(this.getErrors()));
    return umbrellaSpecificationParser.specifications();
  }

  public UmbrellaSpecifications runTypeChecker(ParseTree parseTree) {
    UmbrellaSpecificationTypeChecker umbrellaSpecificationTypeChecker = new UmbrellaSpecificationTypeChecker(
      parseTree,
      this.getSymbolTable(),
      this.getErrors()
    );

    return umbrellaSpecificationTypeChecker.checkTypes();
  }

  public Optional<UmbrellaSpecifications> analyze(String code) {
    return this.analyze(CharStreams.fromString(code));
  }

  @Override
  public Optional<UmbrellaSpecifications> analyze(CharStream code) {
    reset();
    CommonTokenStream tokens;

    switch (this.getCompilerPhase()) {
      case LEXER -> {
        this.runLexer(code);
        return Optional.empty();
      }
      case PARSER -> {
        tokens = this.runLexer(code);
        this.runParser(tokens);
        return Optional.empty();
      }
      case TYPE_CHECKER -> {
        tokens = this.runLexer(code);
        ParseTree parseTree = this.runParser(tokens);

        if (this.hasErrors()) {
          return Optional.empty();
        }

        return Optional.of(this.runTypeChecker(parseTree));
      }
    }
    return Optional.empty();
  }
}
