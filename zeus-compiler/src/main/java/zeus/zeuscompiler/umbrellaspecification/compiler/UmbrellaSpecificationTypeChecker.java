package zeus.zeuscompiler.umbrellaspecification.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;
import zeus.zeuscompiler.umbrellaspecification.compiler.visitors.UmbrellaSpecificationVisitor;

import java.util.List;

public class UmbrellaSpecificationTypeChecker {
  ParseTree parseTree;
  SymbolTable symbolTable;
  List<CompilerError> compilerErrors;

  public UmbrellaSpecificationTypeChecker(
    ParseTree parseTree,
    SymbolTable symbolTable,
    List<CompilerError> compilerErrors
  ) {
    this.parseTree = parseTree;
    this.symbolTable = symbolTable;
    this.compilerErrors = compilerErrors;
  }

  public UmbrellaSpecifications checkTypes() {
    UmbrellaSpecificationVisitor umbrellaSpecificationVisitor = new UmbrellaSpecificationVisitor(this.compilerErrors);
    UmbrellaSpecifications umbrellaSpecifications = (UmbrellaSpecifications) umbrellaSpecificationVisitor.visit(
      parseTree
    );
    umbrellaSpecifications.check(symbolTable, this.compilerErrors);
    return umbrellaSpecifications;
  }
}
