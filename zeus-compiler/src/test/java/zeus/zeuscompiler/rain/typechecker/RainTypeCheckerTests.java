package zeus.zeuscompiler.rain.typechecker;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import zeus.zeuscompiler.rain.compiler.RainAnalyzer;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.UnknownBlueprintComponentException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class RainTypeCheckerTests {
  RainAnalyzer rainAnalyzer;

  @BeforeEach()
  void init() {
    this.rainAnalyzer = new RainAnalyzer(CompilerPhase.TYPE_CHECKER);
  }

  void runAnalyzer(String filename) throws IOException {
    rainAnalyzer.analyze(CharStreams.fromPath(Path.of("src/test/resources/rain/typechecker/" + filename)));
  }

  @Test()
  void test1() throws IOException {
    runAnalyzer("project-1.rain");
    assertThat(rainAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void test2() throws IOException {
    runAnalyzer("project-2.rain");
    assertThat(rainAnalyzer.getErrors().size()).isEqualTo(1);
    assertThat(rainAnalyzer.getErrors().get(0).getException()).isInstanceOf(UnknownBlueprintComponentException.class);
  }

  @Test()
  void test3() throws IOException {
    runAnalyzer("project-3.rain");
    assertThat(rainAnalyzer.hasErrors()).isFalse();
  }

  @Test()
  void test4() throws IOException {
    runAnalyzer("project-4.rain");
    assertThat(rainAnalyzer.hasErrors()).isFalse();
  }
}
