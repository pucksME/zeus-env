package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
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
    }

    public HashMap<String, HashMap<String, String>> translateBootsSpecifications() {
        HashMap<String, HashMap<String, String>> bootsSpecificationTranslations = new HashMap<>();

        for (Route route : this.routes) {
            bootsSpecificationTranslations.put(route.id, route.translateBootsSpecification());
        }

        return bootsSpecificationTranslations;
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

    @Override
    public String translate(int depth, ExportTarget exportTarget) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> String.format(
              CompilerUtils.buildLinesFormat(new String[]{
                "import {app} from './index';",
                "import {bootsMonitorAdapter} from './adapters/boots-monitor.adapter'",
                "import {umbrellaMonitorAdapter} from './adapters/umbrella-monitor.adapter'",
                "%s"
              }, depth),
              this.routes.stream()
                .map(route -> route.translate(depth, exportTarget))
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
