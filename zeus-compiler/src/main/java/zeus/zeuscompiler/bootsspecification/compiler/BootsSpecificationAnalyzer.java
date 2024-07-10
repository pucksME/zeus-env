package zeus.zeuscompiler.bootsspecification.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.Analyzer;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.grammars.BootsSpecificationLexer;
import zeus.zeuscompiler.grammars.BootsSpecificationParser;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderLexerErrorListener;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderParserErrorListener;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Optional;

public class BootsSpecificationAnalyzer extends Analyzer<BootsSpecification> {
  public BootsSpecificationAnalyzer(CompilerPhase compilerPhase) {
    super(compilerPhase);
  }

  @Override
  public CommonTokenStream runLexer(CharStream code) {
    BootsSpecificationLexer bootsSpecificationLexer = new BootsSpecificationLexer(code);
    bootsSpecificationLexer.removeErrorListeners();
    bootsSpecificationLexer.addErrorListener(new ThunderLexerErrorListener(this.getErrors()));
    CommonTokenStream tokens = new CommonTokenStream(bootsSpecificationLexer);
    tokens.fill();
    return tokens;
  }

  @Override
  public ParseTree runParser(CommonTokenStream tokens) {
    BootsSpecificationParser bootsSpecificationParser = new BootsSpecificationParser(tokens);
    bootsSpecificationParser.removeErrorListeners();
    bootsSpecificationParser.addErrorListener(new ThunderParserErrorListener(this.getErrors()));
    return bootsSpecificationParser.specification();
  }

  public BootsSpecification runTypeChecker(ParseTree parseTree) {
    BootsSpecificationTypeChecker bootsSpecificationTypeChecker = new BootsSpecificationTypeChecker(
      parseTree,
      this.getSymbolTable(),
      this.getErrors()
    );

    return bootsSpecificationTypeChecker.checkTypes();
  }

  public Optional<BootsSpecification> analyze(String code) {
    return this.analyze(CharStreams.fromString(code));
  }

  @Override
  public Optional<BootsSpecification> analyze(CharStream code) {
    reset();
    CommonTokenStream tokens;

    switch (this.getCompilerPhase()) {
      case LEXER -> {
        runLexer(code);
        return Optional.empty();
      }
      case PARSER -> {
        tokens = runLexer(code);
        runParser(tokens);
        return Optional.empty();
      }
      case TYPE_CHECKER -> {
        tokens = runLexer(code);
        ParseTree parseTree = runParser(tokens);

        if (this.hasErrors()) {
          return Optional.empty();
        }

        return Optional.of(runTypeChecker(parseTree));
      }
    }
    return Optional.empty();
  }
}
