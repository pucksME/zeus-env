package zeus.zeuscompiler.thunder.compiler;

import org.antlr.v4.runtime.tree.ParseTree;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.visitors.ThunderVisitor;

import java.util.List;

public class ThunderTypeChecker {
  ParseTree parseTree;
  SymbolTable symbolTable;
  List<CompilerError> compilerErrors;

  public ThunderTypeChecker(ParseTree parseTree, SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.parseTree = parseTree;
    this.symbolTable = symbolTable;
    this.compilerErrors = compilerErrors;
  }

  public CodeModules checkTypes() {
    ThunderVisitor thunderVisitor = new ThunderVisitor(this.symbolTable);
    CodeModules codeModules = (CodeModules) thunderVisitor.visit(this.parseTree);
    codeModules.checkTypes(this.symbolTable, this.compilerErrors);
    return codeModules;
  }
}
