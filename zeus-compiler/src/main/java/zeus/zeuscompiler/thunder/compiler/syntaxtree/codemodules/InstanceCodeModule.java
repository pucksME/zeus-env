package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.CodeModuleOutputExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.ConnectionStatement;
import zeus.zeuscompiler.thunder.dtos.CodeModuleDto;
import zeus.zeuscompiler.thunder.dtos.CodeModuleType;
import zeus.zeuscompiler.thunder.dtos.InstanceCodeModuleDto;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.*;
import java.util.stream.Collectors;

public class InstanceCodeModule extends CodeModule {
  public InstanceCodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition, id, description);
  }

  @Override
  public CodeModuleDto toDto() {
    return new InstanceCodeModuleDto(this.id, this.getDescription(), CodeModuleType.INSTANCE);
  }

  Dependency buildDependency(String codeModuleName, Set<Dependency> dependencies) {
    Dependency dependency = new Dependency(codeModuleName);
    dependencies.add(dependency);

    for (BodyComponent bodyComponent : this.body.bodyComponents) {
      ConnectionStatement connectionStatement = (ConnectionStatement) bodyComponent;
      if (connectionStatement.getCodeModuleInputExpression().getCodeModuleId().equals(codeModuleName)) {
        dependency.addConnection(connectionStatement);
      }

      if (!connectionStatement.getCodeModuleOutputExpression().getCodeModuleId().equals(codeModuleName)) {
        continue;
      }

      Dependency existingDependencyCandidate = null;
      for (Dependency existingDependency : dependencies) {
        if (existingDependency.connectionStatements.stream().noneMatch(
          statement -> statement.getCodeModuleInputExpression().getCodeModuleId().equals(
            connectionStatement.getCodeModuleInputExpression().getCodeModuleId()
          )
        )) {
          continue;
        }

        existingDependencyCandidate = existingDependency;
        break;
      }

      if (existingDependencyCandidate != null) {
        dependency.addChild(existingDependencyCandidate);
        continue;
      }

      dependency.addChild(buildDependency(
        connectionStatement.getCodeModuleInputExpression().getCodeModuleId(),
        dependencies
      ));
    }

    return dependency;
  }

  List<Dependency> buildDependencies(SymbolTable symbolTable) {
    Set<Dependency> dependencies = new HashSet<>();
    Set<String> rootCodeModules = symbolTable.getCodeModules().getCodeModules().stream()
      .filter(
        codeModule -> codeModule instanceof ClientCodeModule && ((ClientCodeModule) codeModule).getInputs().size() == 0
      )
      .map(CodeModule::getId)
      .collect(Collectors.toSet());

    List<Dependency> rootDependencies = rootCodeModules.stream().map(
      codeModuleId -> this.buildDependency(codeModuleId, dependencies)
    ).toList();

    return rootDependencies.stream().flatMap(dependency -> dependency.getLeaves().stream()).toList();
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    List<Dependency> dependencies = this.buildDependencies(symbolTable);
    return dependencies.stream().map(
      dependency -> dependency.translate(symbolTable, depth, exportTarget)
    ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 1)));
  }
}
