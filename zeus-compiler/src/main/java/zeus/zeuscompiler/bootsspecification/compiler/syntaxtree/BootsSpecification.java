package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;

import java.util.HashMap;
import java.util.List;

public class BootsSpecification extends Node {
  List<ClassGenerator> classGenerators;

  public BootsSpecification(int line, int linePosition, List<ClassGenerator> classGenerators) {
    super(line, linePosition);
    this.classGenerators = classGenerators;
  }

  public HashMap<String, String> translate() {
    HashMap<String, String> classGeneratorTranslations = new HashMap<>();
    for (ClassGenerator classGenerator : this.classGenerators) {
      classGeneratorTranslations.put(classGenerator.id, classGenerator.translate());
    }
    return classGeneratorTranslations;
  }

  @Override
  public void check(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (ClassGenerator classGenerator : classGenerators) {
      classGenerator.check(symbolTable, compilerErrors);
    }
  }
}
