package zeus.zeuscompiler.thunder.parser;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzerMode;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class ThunderParserTests {
  ThunderAnalyzer thunderAnalyzer;

  @BeforeEach()
  void init() {
    this.thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.PARSER, ThunderAnalyzerMode.CLIENT);
  }

  void runAnalyzer(String filename) throws IOException {
    thunderAnalyzer.analyze(CharStreams.fromPath(
      Path.of("src/test/resources/parser-code-modules/" + filename)
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
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule3() throws IOException {
    runAnalyzer("code-module-3.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule4() throws IOException {
    runAnalyzer("code-module-4.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule5() throws IOException {
    runAnalyzer("code-module-5.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule6() throws IOException {
    runAnalyzer("code-module-6.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule7() throws IOException {
    runAnalyzer("code-module-7.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule8() throws IOException {
    runAnalyzer("code-module-8.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule9() throws IOException {
    runAnalyzer("code-module-9.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule10() throws IOException {
    runAnalyzer("code-module-10.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule11() throws IOException {
    runAnalyzer("code-module-11.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule12() throws IOException {
    runAnalyzer("code-module-12.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule13() throws IOException {
    runAnalyzer("code-module-13.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule14() throws IOException {
    runAnalyzer("code-module-14.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule15() throws IOException {
    runAnalyzer("code-module-15.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule16() throws IOException {
    runAnalyzer("code-module-16.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule17() throws IOException {
    runAnalyzer("code-module-17.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule18() throws IOException {
    runAnalyzer("code-module-18.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule19() throws IOException {
    runAnalyzer("code-module-19.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule20() throws IOException {
    runAnalyzer("code-module-20.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void testCodeModule21() throws IOException {
    runAnalyzer("code-module-21.thunder");
    assertThat(thunderAnalyzer.hasErrors()).isFalse();
  }
}
