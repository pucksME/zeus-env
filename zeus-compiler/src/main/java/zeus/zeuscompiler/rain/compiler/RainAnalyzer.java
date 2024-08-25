package zeus.zeuscompiler.rain.compiler;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.Analyzer;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportProjectDto;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Project;
import zeus.zeuscompiler.grammars.RainLexer;
import zeus.zeuscompiler.grammars.RainParser;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderLexerErrorListener;
import zeus.zeuscompiler.thunder.compiler.errorlisteners.ThunderParserErrorListener;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Optional;

public class RainAnalyzer extends Analyzer<Project> {
  public RainAnalyzer(CompilerPhase compilerPhase) {
    super(compilerPhase);
  }

  void analyzeSemantic(Project project) {
    project.check();
  }


  @Override
  public CommonTokenStream runLexer(CharStream code) {
    RainLexer rainLexer = new RainLexer(code);
    rainLexer.removeErrorListeners();
    rainLexer.addErrorListener(new ThunderLexerErrorListener());
    CommonTokenStream tokens = new CommonTokenStream(rainLexer);
    tokens.fill();
    return tokens;
  }

  @Override
  public ParseTree runParser(CommonTokenStream tokens) {
    RainParser rainParser = new RainParser(tokens);
    rainParser.removeErrorListeners();
    rainParser.addErrorListener(new ThunderParserErrorListener());
    return rainParser.project();
  }

  public Project runTypeChecker(ParseTree parseTree) {
    RainTypeChecker rainTypeChecker = new RainTypeChecker(parseTree);
    return rainTypeChecker.checkTypes();
  }

  @Override
  public Optional<Project> analyze(CharStream code) {
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

        if (ServiceProvider.provide(CompilerErrorService.class).hasErrors()) {
          return Optional.empty();
        }

        return Optional.of(runTypeChecker(parseTree));
      }
    }

    return Optional.empty();
  }

  public Optional<Project> analyze(ExportProjectDto exportProjectDto) {
    return switch (this.getCompilerPhase()) {
      case LEXER, PARSER -> Optional.empty();
      case TYPE_CHECKER -> {
        Project project = Project.fromDto(exportProjectDto);
        this.analyzeSemantic(project);
        if (ServiceProvider.provide(CompilerErrorService.class).hasErrors()) {
          yield Optional.empty();
        }
        yield Optional.of(project);
      }
    };
  }
}
