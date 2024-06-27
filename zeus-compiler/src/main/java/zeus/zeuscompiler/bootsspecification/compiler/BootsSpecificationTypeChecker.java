package zeus.zeuscompiler.bootsspecification.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.bootsspecification.compiler.visitors.BootsSpecificationVisitor;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;

public class BootsSpecificationTypeChecker {
  ParseTree parseTree;
  SymbolTable symbolTable;
  List<CompilerError> compilerErrors;

  public BootsSpecificationTypeChecker(
    ParseTree parseTree,
    SymbolTable symbolTable,
    List<CompilerError> compilerErrors
  ) {
    this.parseTree = parseTree;
    this.symbolTable = symbolTable;
    this.compilerErrors = compilerErrors;
  }

  public BootsSpecification checkTypes() {
    BootsSpecificationVisitor bootsSpecificationVisitor = new BootsSpecificationVisitor(
      this.symbolTable,
      this.compilerErrors
    );
    BootsSpecification bootsSpecification = (BootsSpecification) bootsSpecificationVisitor.visit(this.parseTree);
    bootsSpecification.check(this.symbolTable, this.compilerErrors);
    return bootsSpecification;
  }
}
