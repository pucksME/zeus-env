package zeus.zeuscompiler.bootsspecification.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.bootsspecification.compiler.visitors.BootsSpecificationVisitor;

import java.util.List;

public class BootsSpecificationTypeChecker {
  ParseTree parseTree;

  public BootsSpecificationTypeChecker(ParseTree parseTree) {
    this.parseTree = parseTree;
  }

  public BootsSpecification checkTypes() {
    BootsSpecificationVisitor bootsSpecificationVisitor = new BootsSpecificationVisitor();
    BootsSpecification bootsSpecification = (BootsSpecification) bootsSpecificationVisitor.visit(this.parseTree);
    bootsSpecification.check();
    return bootsSpecification;
  }
}
