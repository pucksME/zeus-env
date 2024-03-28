package zeus.zeuscompiler.thunder.lexer;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class ThunderLexerTests {
  ThunderAnalyzer thunderAnalyzer;

  @BeforeEach()
  void init() {
    this.thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.LEXER);
  }

  void runAnalyzer(String filename) throws IOException {
    thunderAnalyzer.analyze(CharStreams.fromPath(
      Path.of("src/test/resources/lexer-code-modules/" + filename)
    ));
  }

  @Test()
  void testCodeModule1() throws IOException {
    runAnalyzer("code-module-1.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule2() throws IOException {
    runAnalyzer("code-module-2.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isTrue();
  }
}
