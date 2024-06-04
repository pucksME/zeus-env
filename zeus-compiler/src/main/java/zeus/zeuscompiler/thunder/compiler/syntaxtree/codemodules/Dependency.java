package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.CodeModuleOutputExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.ConnectionStatement;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Dependency implements Translatable {
  String codeModuleName;
  List<Dependency> parents;
  List<Dependency> children;
  List<ConnectionStatement> connectionStatements;
  boolean translated;

  public Dependency(String codeModuleName) {
    this.codeModuleName = codeModuleName;
    this.parents = new ArrayList<>();
    this.children = new ArrayList<>();
    this.connectionStatements = new ArrayList<>();
    this.translated = false;
  }

  void addChild(Dependency dependency) {
    dependency.parents.add(this);
    this.children.add(dependency);
  }

  void addConnection(ConnectionStatement connectionStatement) {
    this.connectionStatements.add(connectionStatement);
  }

  List<Dependency> getLeaves() {
    if (this.children.size() == 0) {
      return Collections.singletonList(this);
    }

    return this.children.stream().flatMap(dependency -> dependency.getLeaves().stream()).toList();
  }

  String translateResponseAccess(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> this.connectionStatements.stream()
        .map(connectionStatement -> String.format(
          "res['body'] = %s_%s",
          connectionStatement.getCodeModuleOutputExpression().getCodeModuleId(),
          connectionStatement.getCodeModuleOutputExpression().getOutputId()
        ))
        .collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 1)));
    };
  }

  String translateRequestAccess(SymbolTable symbolTable, ClientCodeModule clientCodeModule, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> clientCodeModule.getOutputs().stream()
        .map(output -> String.format(
          "const request_%s = req['%s'] as %s",
          output.getId(),
          (output.getId().equals("url") ? "params" : "body"),
          output.type.translate(symbolTable, depth, exportTarget)
        ))
        .collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 1)));
    };
  }

  String translateOutputs(ClientCodeModule clientCodeModule, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "const {%s}",
        clientCodeModule.getOutputs().stream().map(output -> String.format(
          "%s_%s",
          clientCodeModule.getId(),
          output.getId()
        )).collect(Collectors.joining(", "))
      );
    };
  }

  String translateInputs(ClientCodeModule clientCodeModule, ExportTarget exportTarget) {
    List<Input> inputs = clientCodeModule.getInputs().stream().toList();
    assert inputs.size() == this.connectionStatements.size();
    List<String> parameters = new ArrayList<>();

    for (Input input : inputs) {
      for (ConnectionStatement connectionStatement : this.connectionStatements) {
        if (connectionStatement.getCodeModuleInputExpression().getInputId().equals(input.getId())) {
          CodeModuleOutputExpression codeModuleOutputExpression = connectionStatement.getCodeModuleOutputExpression();
          parameters.add(String.format(
            "%s_%s",
            codeModuleOutputExpression.getCodeModuleId(),
            codeModuleOutputExpression.getOutputId())
          );
        }
      }
    }

    assert parameters.size() == inputs.size();
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.join(", ", parameters);
    };
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    List<String> translations = new ArrayList<>(this.parents.stream().map(
      dependency -> dependency.translate(symbolTable, depth, exportTarget)
    ).toList());

    Optional<CodeModule> codeModuleOptional = symbolTable.getCodeModules().getCodeModule(this.codeModuleName);
    assert codeModuleOptional.isPresent();
    ClientCodeModule clientCodeModule = (ClientCodeModule) codeModuleOptional.get();

    if (clientCodeModule.hasEmptyTranslation()){
      return "";
    }

    if (!this.translated) {
      if (clientCodeModule instanceof RequestCodeModule) {
        translations.add(this.translateRequestAccess(symbolTable, clientCodeModule, depth, exportTarget));
      } else if (clientCodeModule instanceof ResponseCodeModule) {
        translations.add(this.translateResponseAccess(depth, exportTarget));
      } else {
        translations.add(switch (exportTarget) {
          case REACT_TYPESCRIPT -> String.format(
            "%s = %s(%s);",
            this.translateOutputs(clientCodeModule, exportTarget),
            clientCodeModule.getId(),
            (this.connectionStatements.size() != 0) ? this.translateInputs(clientCodeModule, exportTarget) : ""
          );
        });
      }
    }

    this.translated = true;
    return String.join("\n" + CompilerUtils.buildLinePadding(depth + 1), translations);
  }
}
