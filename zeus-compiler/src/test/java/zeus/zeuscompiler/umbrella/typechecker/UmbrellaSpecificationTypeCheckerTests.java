package zeus.zeuscompiler.umbrella.typechecker;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.UmbrellaSpecificationAnalyzer;

import java.io.IOException;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class UmbrellaSpecificationTypeCheckerTests {
  UmbrellaSpecificationAnalyzer umbrellaSpecificationAnalyzer;

  @BeforeEach()
  void init() {
    ServiceProvider.initialize();
    ServiceProvider.register(new CompilerErrorService());
    ServiceProvider.register(new SymbolTableService());
    this.umbrellaSpecificationAnalyzer = new UmbrellaSpecificationAnalyzer(CompilerPhase.TYPE_CHECKER);
  }

  void runAnalyzer(String filename) throws IOException {
    umbrellaSpecificationAnalyzer.analyze(CharStreams.fromPath(Path.of(
      "src/test/resources/umbrella-specification/typechecker/" + filename
    )));
  }

  @Test()
  void test1() throws IOException {
    runAnalyzer("formula-1.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test2() throws IOException {
    runAnalyzer("formula-2.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test3() throws IOException {
    runAnalyzer("formula-3.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test4() throws IOException {
    runAnalyzer("formula-4.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test5() throws IOException {
    runAnalyzer("formula-5.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test6() throws IOException {
    runAnalyzer("formula-6.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test7() throws IOException {
    runAnalyzer("formula-7.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test8() throws IOException {
    runAnalyzer("formula-8.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test9() throws IOException {
    runAnalyzer("formula-9.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test10() throws IOException {
    runAnalyzer("formula-10.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test11() throws IOException {
    runAnalyzer("formula-11.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test12() throws IOException {
    runAnalyzer("formula-12.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test13() throws IOException {
    runAnalyzer("formula-13.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test14() throws IOException {
    runAnalyzer("formula-14.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test15() throws IOException {
    runAnalyzer("formula-15.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test16() throws IOException {
    runAnalyzer("formula-16.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test17() throws IOException {
    runAnalyzer("formula-17.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test18() throws IOException {
    runAnalyzer("formula-18.umbrella");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }
}
