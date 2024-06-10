package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.stream.Collectors;

public class Server extends Node {
    String address;
    int port;
    List<Route> routes;

    public Server(int line, int linePosition, String name, String address, int port, List<Route> routes) {
        super(line, linePosition, name);
        this.address = address;
        this.port = port;
        this.routes = routes;
    }

    @Override
    public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> String.format(
              CompilerUtils.buildLinesFormat(new String[]{
                "import {app} from './index.ts';",
                "export const name = \"%s\";",
                "export const address = \"%s\";",
                "export const port = \"%s\";",
                "%s"
              }, depth),
              this.name,
              this.address,
              this.port,
              this.routes.stream()
                .map(route -> route.translate(symbolTable, depth, exportTarget))
                .collect(Collectors.joining("\n"))
            );
        };
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
        for (Route route : this.routes) {
            route.check(symbolTable, compilerErrors);
        }
    }
}
