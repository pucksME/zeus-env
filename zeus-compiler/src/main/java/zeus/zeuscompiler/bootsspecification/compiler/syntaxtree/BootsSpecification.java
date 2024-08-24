package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree;

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
  public void check() {
    for (ClassGenerator classGenerator : classGenerators) {
      classGenerator.check();
    }
  }
}
