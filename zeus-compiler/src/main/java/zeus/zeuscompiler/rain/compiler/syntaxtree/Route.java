package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ServerRouteSymbolTable;
import zeus.zeuscompiler.symboltable.SymbolTable;
import zeus.zeuscompiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Output;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class Route extends Node {
    String id;
    RouteMethod routeMethod;
    CodeModules codeModules;
    BootsSpecification bootsSpecification;
    UmbrellaSpecifications umbrellaSpecifications;
    Server server;

    public Route(
      int line,
      int linePosition,
      String id,
      RouteMethod routeMethod,
      CodeModules codeModules,
      BootsSpecification bootsSpecification,
      UmbrellaSpecifications umbrellaSpecifications
    ) {
        super(line, linePosition, "");
        this.id = id;
        this.routeMethod = routeMethod;
        this.codeModules = codeModules;
        this.bootsSpecification = bootsSpecification;
        this.umbrellaSpecifications = umbrellaSpecifications;
    }

    public HashMap<String, String> translateBootsSpecification() {
        return (this.bootsSpecification != null) ? this.bootsSpecification.translate() : new HashMap<>();
    }

    public HashMap<String, String> translateUmbrellaSpecification(String serverName, String routeId) {
        return (this.umbrellaSpecifications != null) ? this.umbrellaSpecifications.translate(serverName, routeId) : new HashMap<>();
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

    public String translateTypingMiddleware(ExportTarget exportTarget, int depth) {
        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> {
                Optional<RequestCodeModule> requestCodeModuleOptional = ServiceProvider
                  .provide(SymbolTableService.class).getContextSymbolTableProvider()
                  .provide(ServerRouteSymbolTable.class).getRoutingCodeModule(RequestCodeModule.class);

                yield String.format(
                  CompilerUtils.buildLinesFormat(
                    new String[]{
                      "if (routeId === '%s') {",
                      "%s",
                      CompilerUtils.buildLinePadding(depth + 1) + "next();",
                      CompilerUtils.buildLinePadding(depth + 1) + "return;",
                      "}"
                    },
                    depth
                  ),
                  this.id,
                  (requestCodeModuleOptional.isPresent())
                    ? requestCodeModuleOptional.get().translateTypingMiddleware(exportTarget, depth + 1)
                    : ""
                );
            }
        };
    }

    private String translateMiddlewares(ExportTarget exportTarget) {
        switch (exportTarget) {
            case REACT_TYPESCRIPT -> {
                List<String> middlewares = new ArrayList<>();

                middlewares.add(String.format("(req, res, next) => typingMiddleware(req, res, next, '%s')", this.id));

                return (middlewares.isEmpty()) ? "" : String.join(", ", middlewares) + ", ";
            }
        }
        throw new RuntimeException("Could not translate middlewares: unsupported export target");
    }

    private String translateMonitorAdapters() {
        List<String> translatedAdapters = new ArrayList<>();

        if (this.bootsSpecification != null) {
            translatedAdapters.add(String.format(
              "(req, res, next) => bootsMonitorAdapter('%s', req, res, next)", this.id
            ));
        }

        if (this.server == null) {
            throw new RuntimeException(String.format(
              "Could not translate monitor adapters of route \"%s\": unknown server",
              this.id
            ));
        }

        if (this.umbrellaSpecifications != null) {
            translatedAdapters.add(String.format(
              "(req, res, next) => umbrellaMonitorAdapter('%s', '%s', req, res, next)",
              this.server.name,
              this.id
            ));
        }

        if (translatedAdapters.isEmpty()) {
            return "";
        }

        return String.format("[%s]", String.join(", ", translatedAdapters)) + ", ";
    }

    public void setServer(Server server) {
        this.server = server;
    }

    @Override
    public String translate(int depth, ExportTarget exportTarget) {
        Optional<CodeModule> codeModuleOptional = this.codeModules.getCodeModule("request");
        String parameters = "";
        if (codeModuleOptional.isPresent() && codeModuleOptional.get() instanceof RequestCodeModule) {
            Optional<Output> outputOptional = ((RequestCodeModule) codeModuleOptional.get()).getOutput("url");
            if (outputOptional.isPresent()) {
                Output output = outputOptional.get();

                if (output.getType() instanceof ObjectType) {
                    parameters = ((ObjectType) outputOptional.get().getType()).translateToUrlParameters(exportTarget);
                } else if (output.getType() instanceof IdType) {
                    Optional<TypeInformation> typeInformationOptional = ServiceProvider
                      .provide(SymbolTableService.class).getContextSymbolTableProvider()
                      .provide(SymbolTable.class).getType(
                        codeModuleOptional.get(),
                        ((IdType) output.getType()).getId()
                      );

                    if(typeInformationOptional.isPresent()) {
                        Type type = typeInformationOptional.get().getType();
                        if (type instanceof ObjectType) {
                            parameters = ((ObjectType) typeInformationOptional.get().getType()).translateToUrlParameters(exportTarget);
                        }
                    }
                }
            }
        }

        return switch (exportTarget) {
            case REACT_TYPESCRIPT -> String.format(
              CompilerUtils.buildLinesFormat(
                new String[]{
                  "app.%s('/%s%s', %sfunction(req, res) {",
                  CompilerUtils.buildLinePadding(depth + 1) + "%s",
                  "});"
                },
                depth
              ),
              this.translateRequestMethod(exportTarget),
              this.id,
              "/" + parameters,
              this.translateMiddlewares(exportTarget) + this.translateMonitorAdapters(),
              this.codeModules.translate(depth, exportTarget)
            );
        };
    }

    @Override
    public void check() {
    }

    public String getId() {
        return id;
    }
}
