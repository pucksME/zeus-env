package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousClassException;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.ServerSymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClassGenerator extends Node {
  String id;
  String generator;

  public ClassGenerator(int line, int linePosition, String id, String generator) {
    super(line, linePosition);
    this.id = id;
    this.generator = generator;
  }

  public String translate() {
    int firstNonBlankLine = -1;
    int lastNonBlankLine = -1;
    String[] lines = this.generator.split("\n");
    for (int i = 0; i < lines.length; i++) {
      if (firstNonBlankLine == -1 && !lines[i].isBlank()) {
        firstNonBlankLine = i;
      }
      if (!lines[i].isBlank()) {
        lastNonBlankLine = i;
      }
    }

    int whitespaceToRemove = Arrays.stream(this.generator.split("\n"))
      .filter(line -> !line.isBlank())
      .map(line -> line.length() - line.stripLeading().length())
      .min(Integer::compareTo).orElse(0);

    return ((firstNonBlankLine == -1 || lastNonBlankLine == -1)
      ? Arrays.stream(lines).toList()
      : Arrays.stream(lines).toList().subList(firstNonBlankLine, lastNonBlankLine + 1)).stream()
      .map(line -> line.substring(whitespaceToRemove))
      .collect(Collectors.joining("\n"));
  }

  @Override
  public void check() {
    if (ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerSymbolTable.class).getBootsSpecificationClasses().stream()
      .filter(bootsSpecificationClass -> bootsSpecificationClass.equals(this.id))
      .count() > 1) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.line,
        this.linePosition,
        new AmbiguousClassException(this.id),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }
}
