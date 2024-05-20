package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;

import java.util.List;

public class Route extends Node {
    String id;
    RouteMethod routeMethod;
    CodeModules codeModules;

    public Route(int line, int linePosition, String id, RouteMethod routeMethod, CodeModules codeModules) {
        super(line, linePosition, "");
        this.id = id;
        this.routeMethod = routeMethod;
        this.codeModules = codeModules;
    }

    @Override
    public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
        return this.codeModules.translate(symbolTable, depth, exportTarget);
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    }
}
