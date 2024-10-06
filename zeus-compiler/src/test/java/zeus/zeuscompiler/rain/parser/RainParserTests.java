package zeus.zeuscompiler.rain.parser;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.compiler.RainAnalyzer;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class RainParserTests {
  RainAnalyzer rainAnalyzer;

  @BeforeEach()
  void init() {
    ServiceProvider.initialize();
    ServiceProvider.register(new CompilerErrorService());
    ServiceProvider.register(new SymbolTableService());
    this.rainAnalyzer = new RainAnalyzer(CompilerPhase.PARSER);
  }

  void runAnalyzer(String filename) throws IOException {
    rainAnalyzer.analyze(CharStreams.fromPath(Path.of("src/test/resources/rain/parser/" + filename)));
  }

  @Test()
  void test1() throws IOException {
    runAnalyzer("project-1.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test2() throws IOException {
    runAnalyzer("project-2.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test3() throws IOException {
    runAnalyzer("project-3.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test4() throws IOException {
    runAnalyzer("project-4.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test5() throws IOException {
    runAnalyzer("project-5.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test6() throws IOException {
    runAnalyzer("project-6.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test7() throws IOException {
    runAnalyzer("project-7.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }
}
