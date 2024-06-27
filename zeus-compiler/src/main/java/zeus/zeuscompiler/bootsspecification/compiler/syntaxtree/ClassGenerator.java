package zeus.zeuscompiler.bootsspecification.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousClassException;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;

public class ClassGenerator extends Node {
  String id;
  String generator;

  public ClassGenerator(int line, int linePosition, String id, String generator) {
    super(line, linePosition);
    this.id = id;
    this.generator = generator;
  }

  @Override
  public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (symbolTable.getBootsSpecificationClasses().stream().filter(bootsSpecificationClass -> bootsSpecificationClass.equals(this.id)).count() > 1) {
      compilerErrors.add(new CompilerError(
        this.line,
        this.linePosition,
        new AmbiguousClassException(this.id),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }
}
