package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;

import java.util.List;
import java.util.stream.Collectors;

public class Server extends Node {
    String ip;
    int port;
    List<Route> routes;

    public Server(int line, int linePosition, String ip, int port, List<Route> routes) {
        super(line, linePosition, "");
        this.ip = ip;
        this.port = port;
        this.routes = routes;
    }

    @Override
    public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
        return this.routes.stream()
          .map(route -> route.translate(symbolTable, depth, exportTarget))
          .collect(Collectors.joining("\n"));
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
        for (Route route : this.routes) {
            route.check(symbolTable, compilerErrors);
        }
    }
}
