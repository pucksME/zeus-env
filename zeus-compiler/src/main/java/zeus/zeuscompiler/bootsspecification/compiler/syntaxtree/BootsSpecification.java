package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;

public class BootsSpecification extends Node {
  List<ClassGenerator> classGenerators;

  public BootsSpecification(int line, int linePosition, List<ClassGenerator> classGenerators) {
    super(line, linePosition);
    this.classGenerators = classGenerators;
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (ClassGenerator classGenerator : classGenerators) {
      classGenerator.check(symbolTable, compilerErrors);
    }
  }
}
