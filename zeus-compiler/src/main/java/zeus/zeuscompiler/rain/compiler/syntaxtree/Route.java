package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Output;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecifications;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Route extends Node {
    String id;
    RouteMethod routeMethod;
    CodeModules codeModules;
    BootsSpecification bootsSpecification;
    UmbrellaSpecifications umbrellaSpecifications;

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

    private String translateMonitorAdapters() {
        List<String> translatedAdapters = new ArrayList<>();

        if (this.bootsSpecification != null) {
            translatedAdapters.add(String.format("(req, res, next) => bootsMonitorAdapter('%s', req, res, next)", this.id));
        }

        translatedAdapters.add(String.format("(req, res, next) => umbrellaMonitorAdapter('%s', req, res, next)", this.id));

        if (translatedAdapters.isEmpty()) {
            return "";
        }

        return String.format("[%s]", String.join(", ", translatedAdapters)) + ", ";
    }

    @Override
    public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
        Optional<CodeModule> codeModuleOptional = this.codeModules.getCodeModule("request");
        String parameters = "";
        if (codeModuleOptional.isPresent() && codeModuleOptional.get() instanceof RequestCodeModule) {
            Optional<Output> outputOptional = ((RequestCodeModule) codeModuleOptional.get()).getOutput("url");
            if (outputOptional.isPresent()) {
                Output output = outputOptional.get();

                if (output.getType() instanceof ObjectType) {
                    parameters = ((ObjectType) outputOptional.get().getType()).translateToUrlParameters(exportTarget);
                } else if (output.getType() instanceof IdType) {
                    Optional<TypeInformation> typeInformationOptional = symbolTable.getType(
                      codeModuleOptional.get(),
                      ((IdType) output.getType()).getId()
                    );

                    if(typeInformationOptional.isPresent()) {
                        parameters = typeInformationOptional.get().getType().translateToUrlParameters(exportTarget);
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
              this.translateMonitorAdapters(),
              this.codeModules.translate(symbolTable, depth, exportTarget)
            );
        };
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    }
}
