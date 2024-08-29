package zeus.zeuscompiler.thunder.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.visitors.ThunderVisitor;


public class ThunderTypeChecker {
  ParseTree parseTree;
  ThunderAnalyzerMode thunderAnalyzerMode;

  public ThunderTypeChecker(ParseTree parseTree, ThunderAnalyzerMode thunderAnalyzerMode) {
    this.parseTree = parseTree;
    this.thunderAnalyzerMode = thunderAnalyzerMode;
  }

  public CodeModules checkTypes() {
    ThunderVisitor thunderVisitor = new ThunderVisitor(this.thunderAnalyzerMode);
    CodeModules codeModules = (CodeModules) thunderVisitor.visit(this.parseTree);
    codeModules.checkTypes();
    return codeModules;
  }
}
