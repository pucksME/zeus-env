package zeus.zeuscompiler.rain.typechecker;

import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.compiler.RainAnalyzer;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.UnknownBlueprintComponentException;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.CodeModuleComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnsupportedCodeModuleComponentsException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;

@SpringBootTest
public class RainTypeCheckerTests {
  RainAnalyzer rainAnalyzer;

  @BeforeEach()
  void init() {
    ServiceProvider.initialize();
    ServiceProvider.register(new CompilerErrorService());
    ServiceProvider.register(new SymbolTableService());
    this.rainAnalyzer = new RainAnalyzer(CompilerPhase.TYPE_CHECKER);
  }

  void runAnalyzer(String filename) throws IOException {
    rainAnalyzer.analyze(CharStreams.fromPath(Path.of("src/test/resources/rain/typechecker/" + filename)));
  }

  @Test()
  void test1() throws IOException {
    runAnalyzer("project-1.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }

  @Test()
  void test2() throws IOException {
    runAnalyzer("project-2.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).isInstanceOf(UnknownBlueprintComponentException.class);
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
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(7);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(12);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).isInstanceOf(IncompatibleTypeException.class);
  }

  @Test()
  void test6() throws IOException {
    runAnalyzer("project-6.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(7);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(12);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).isInstanceOf(IncompatibleTypeException.class);
  }

  @Test()
  void test7() throws IOException {
    runAnalyzer("project-7.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(7);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(12);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).isInstanceOf(IncompatibleTypeException.class);
  }

  @Test()
  void test8() throws IOException {
    runAnalyzer("project-8.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(6);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(8);
    assertThat(
      ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
    ).isInstanceOf(UnsupportedCodeModuleComponentsException.class);
    assertThat(
      ((UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).getCodeModuleId()
    ).isEqualTo("request");
    assertThat(
      (
        (UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
      ).getCodeModuleComponent()
    ).isEqualTo(CodeModuleComponent.INPUT);
  }

  @Test()
  void test9() throws IOException {
    runAnalyzer("project-9.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(6);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(8);
    assertThat(
      ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
    ).isInstanceOf(UnsupportedCodeModuleComponentsException.class);
    assertThat(
      ((UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).getCodeModuleId()
    ).isEqualTo("request");
    assertThat(
      (
        (UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
      ).getCodeModuleComponent()
    ).isEqualTo(CodeModuleComponent.BODY);
  }

  @Test()
  void test10() throws IOException {
    runAnalyzer("project-10.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(6);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(8);
    assertThat(
      ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
    ).isInstanceOf(UnsupportedCodeModuleComponentsException.class);
    assertThat(
      ((UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).getCodeModuleId()
    ).isEqualTo("response");
    assertThat(
      (
        (UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
      ).getCodeModuleComponent()
    ).isEqualTo(CodeModuleComponent.OUTPUT);
  }

  @Test()
  void test11() throws IOException {
    runAnalyzer("project-11.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().size()).isEqualTo(1);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLine()).isEqualTo(6);
    assertThat(ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getLinePosition()).isEqualTo(8);
    assertThat(
      ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
    ).isInstanceOf(UnsupportedCodeModuleComponentsException.class);
    assertThat(
      ((UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()).getCodeModuleId()
    ).isEqualTo("response");
    assertThat(
      (
        (UnsupportedCodeModuleComponentsException) ServiceProvider.provide(CompilerErrorService.class).getErrors().get(0).getException()
      ).getCodeModuleComponent()
    ).isEqualTo(CodeModuleComponent.BODY);
  }

  @Test()
  void test12() throws IOException {
    runAnalyzer("project-12.rain");
    assertThat(ServiceProvider.provide(CompilerErrorService.class).hasErrors()).isFalse();
  }
}
