package zeus.zeuscompiler.thunder.typechecker;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;
import zeus.zeuscompiler.thunder.compiler.ThunderAnalyzer;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.InvalidAssignmentException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.MissingDeclarationException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class ThunderTypeCheckerTests {
  ThunderAnalyzer thunderAnalyzer;

  @BeforeEach
  void inti() {
    this.thunderAnalyzer = new ThunderAnalyzer(CompilerPhase.TYPE_CHECKER);
  }

  void runAnalyzer(String filename) throws IOException {
    thunderAnalyzer.analyze(CharStreams.fromPath(
      Path.of("src/test/resources/type-checker-code-modules/" + filename)
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
    assertThat(thunderAnalyzer.getErrors().size()).isEqualTo(1);
    assertThat(thunderAnalyzer.getErrors().get(0).getException()).isInstanceOf(InvalidAssignmentException.class);
  }

  @Test()
  void testCodeModule6() throws IOException {
    runAnalyzer("code-module-6.thunder");
    assertThat(thunderAnalyzer.getErrors().size()).isEqualTo(1);
    assertThat(thunderAnalyzer.getErrors().get(0).getException()).isInstanceOf(InvalidAssignmentException.class);
  }

  @Test()
  void testCodeModule7() throws IOException {
    runAnalyzer("code-module-7.thunder");
    assertThat(thunderAnalyzer.getErrors().size()).isEqualTo(1);
    assertThat(thunderAnalyzer.getErrors().get(0).getException()).isInstanceOf(MissingDeclarationException.class);
  }
}
