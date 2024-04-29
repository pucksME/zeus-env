package zeus.zeuscompiler.rain.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.compiler.syntaxtree.Project;
import zeus.zeuscompiler.rain.compiler.visitors.RainVisitor;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;

public class RainTypeChecker {
  ParseTree parseTree;
  SymbolTable symbolTable;
  List<CompilerError> compilerErrors;

  public RainTypeChecker(ParseTree parseTree, SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.parseTree = parseTree;
    this.symbolTable = symbolTable;
    this.compilerErrors = compilerErrors;
  }

  public Project checkTypes() {
    RainVisitor rainVisitor = new RainVisitor(this.symbolTable, this.compilerErrors);
    Project project = (Project) rainVisitor.visit(parseTree);
    // TODO: perform type checking
    project.check(this.symbolTable, this.compilerErrors);
    return project;
  }
}
