package zeus.zeuscompiler.thunder.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.visitors.ThunderVisitor;

import java.util.List;

public class ThunderTypeChecker {
  ParseTree parseTree;
  ClientSymbolTable symbolTable;
  List<CompilerError> compilerErrors;
  ThunderAnalyzerMode thunderAnalyzerMode;

  public ThunderTypeChecker(ParseTree parseTree, ThunderAnalyzerMode thunderAnalyzerMode) {
    this.parseTree = parseTree;
    this.thunderAnalyzerMode = thunderAnalyzerMode;
  }

  public CodeModules checkTypes() {
    ThunderVisitor thunderVisitor = new ThunderVisitor(this.symbolTable, this.thunderAnalyzerMode);
    CodeModules codeModules = (CodeModules) thunderVisitor.visit(this.parseTree);
    codeModules.checkTypes();
    return codeModules;
  }
}
