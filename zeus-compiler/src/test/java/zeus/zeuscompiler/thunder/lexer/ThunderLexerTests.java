package zeus.zeuscompiler.thunder.lexer;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzerMode;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class ThunderLexerTests {
  ThunderAnalyzer thunderAnalyzer;

  @BeforeEach()
  void init() {
    ServiceProvider.initialize();
    ServiceProvider.register(new CompilerErrorService());
    ServiceProvider.register(new SymbolTableService());
    this.thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.LEXER, ThunderAnalyzerMode.CLIENT);
  }

  void runAnalyzer(String filename) throws IOException {
    thunderAnalyzer.analyze(CharStreams.fromPath(
      Path.of("src/test/resources/lexer-code-modules/" + filename)
    ));
  }

  @Test()
  void testCodeModule1() throws IOException {
    runAnalyzer("code-module-1.thunder");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule2() throws IOException {
    runAnalyzer("code-module-2.thunder");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isTrue();
  }
}
