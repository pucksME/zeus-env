package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.utils.CompilerUtils;

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

    private String translateRequestMethod(ExportTarget exportTarget) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> switch (this.routeMethod) {
                case DELETE -> "delete";
                case GET -> "get";
                case POST -> "post";
                case UPDATE -> "update";
            };
        };
    }

    @Override
    public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> String.format(
              CompilerUtils.buildLinesFormat(
                new String[]{
                  "function app.%s('/%s', function(req, res) {",
                  CompilerUtils.buildLinePadding(depth + 1) + "%s",
                  "});"
                },
                depth
              ),
              this.translateRequestMethod(exportTarget),
              this.id,
              this.codeModules.translate(symbolTable, depth, exportTarget)
            );
        };
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    }
}
