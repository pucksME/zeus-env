package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTableIdentifier;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTableIdentifier;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.HashMap;
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
        this.routes.forEach(route -> route.setServer(this));
    }

    public HashMap<String, HashMap<String, String>> translateBootsSpecifications() {
        HashMap<String, HashMap<String, String>> bootsSpecificationTranslations = new HashMap<>();

        for (Route route : this.routes) {
            bootsSpecificationTranslations.put(route.id, route.translateBootsSpecification());
        }

        return bootsSpecificationTranslations;
    }

    public HashMap<String, HashMap<String, String>> translateUmbrellaSpecifications() {
        HashMap<String, HashMap<String, String>> umbrellaSpecificationTranslations = new HashMap<>();

        for (Route route : this.routes) {
            ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ServerRouteSymbolTableIdentifier(this.name, route.id));
            umbrellaSpecificationTranslations.put(route.id, route.translateUmbrellaSpecification(this.name, route.id));
        }

        ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ClientSymbolTableIdentifier());
        return umbrellaSpecificationTranslations;
    }

    public String translateConfiguration(int depth, ExportTarget exportTarget) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> String.format(
              CompilerUtils.buildLinesFormat(new String[]{
                "export const name = \"%s\";",
                "export const address = \"%s\";",
                "export const port = \"%s\";",
              }, depth),
              this.name,
              this.address,
              this.port
            );
        };
    }

    public String translateTypingMiddleware(ExportTarget exportTarget, int depth) {
        return this.routes.stream()
          .map(route -> {
              ServiceProvider
                .provide(SymbolTableService.class)
                .setContextSymbolTable(new ServerRouteSymbolTableIdentifier(this.name, route.getId()));

              String translation = route.translateTypingMiddleware(exportTarget, depth);

              ServiceProvider
                .provide(SymbolTableService.class)
                .setContextSymbolTable(new ClientSymbolTableIdentifier());

              return CompilerUtils.buildLinePadding(depth) + translation;
          })
          .collect(Collectors.joining("\n"));
    }

    @Override
    public String translate(int depth, ExportTarget exportTarget) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> String.format(
              CompilerUtils.buildLinesFormat(new String[]{
                "import {app} from './index';",
                "import {typingMiddleware} from '../middlewares/typing.middleware'",
                "import {bootsMonitorAdapter} from './adapters/boots-monitor.adapter'",
                "import {umbrellaMonitorAdapter} from './adapters/umbrella-monitor.adapter'",
                "%s"
              }, depth),
              this.routes.stream()
                .map(route -> {
                    ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ServerRouteSymbolTableIdentifier(this.name, route.id));
                    String translation = route.translate(depth, exportTarget);
                    ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ClientSymbolTableIdentifier());
                    return translation;
                })
                .collect(Collectors.joining("\n"))
            );
        };
    }

    @Override
    public void check() {
        for (Route route : this.routes) {
            route.check();
        }
    }
}
