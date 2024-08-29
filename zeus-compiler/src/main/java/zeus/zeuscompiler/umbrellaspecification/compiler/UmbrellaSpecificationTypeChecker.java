package zeus.zeuscompiler.umbrellaspecification.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;
import zeus.zeuscompiler.umbrellaspecification.compiler.visitors.UmbrellaSpecificationVisitor;


public class UmbrellaSpecificationTypeChecker {
  ParseTree parseTree;

  public UmbrellaSpecificationTypeChecker(ParseTree parseTree) {
    this.parseTree = parseTree;
  }

  public UmbrellaSpecifications checkTypes() {
    UmbrellaSpecificationVisitor umbrellaSpecificationVisitor = new UmbrellaSpecificationVisitor();
    UmbrellaSpecifications umbrellaSpecifications = (UmbrellaSpecifications) umbrellaSpecificationVisitor.visit(
      parseTree
    );
    umbrellaSpecifications.check();
    return umbrellaSpecifications;
  }
}
