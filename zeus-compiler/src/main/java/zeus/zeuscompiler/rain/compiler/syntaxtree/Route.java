package zeus.zeuscompiler.rain.compiler.syntaxtree;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.symboltable.TypeInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModules;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Output;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.RequestCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.IdType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.Optional;

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
                  "function app.%s('/%s%s', function(req, res) {",
                  CompilerUtils.buildLinePadding(depth + 1) + "%s",
                  "});"
                },
                depth
              ),
              this.translateRequestMethod(exportTarget),
              this.id,
              "/" + parameters,
              this.codeModules.translate(symbolTable, depth, exportTarget)
            );
        };
    }

    @Override
    public void check(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    }
}
