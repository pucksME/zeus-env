package zeus.zeuscompiler.rain.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Project;
import zeus.zeuscompiler.rain.compiler.visitors.RainVisitor;


public class RainTypeChecker {
  ParseTree parseTree;

  public RainTypeChecker(ParseTree parseTree) {
    this.parseTree = parseTree;
  }

  public Project checkTypes() {
    RainVisitor rainVisitor = new RainVisitor();
    Project project = (Project) rainVisitor.visit(parseTree);
    project.check();
    return project;
  }
}
