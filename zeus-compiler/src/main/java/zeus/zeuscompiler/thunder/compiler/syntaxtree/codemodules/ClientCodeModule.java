package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.dtos.ClientCodeModuleDto;
import zeus.zeuscompiler.thunder.dtos.CodeModuleDto;
import zeus.zeuscompiler.thunder.dtos.CodeModuleType;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientCodeModule extends CodeModule {
  Head head;

  public ClientCodeModule() {
    super(-1, -1, "Unnamed", "");
    this.head = new Head(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    this.body = new Body(new ArrayList<>());
  }

  public ClientCodeModule(int line, int linePosition, String id, String description) {
    super(line, linePosition, id, description);
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    for (Input input : this.head.inputs.values()) {
      input.checkTypes(symbolTable, compilerErrors);
    }

    for (Output output : this.head.outputs.values()) {
      output.checkTypes(symbolTable, compilerErrors);
    }

    for (Config config : this.head.configs.values()) {
      config.checkTypes(symbolTable, compilerErrors);
    }

    super.checkTypes(symbolTable, compilerErrors);
  }

  @Override
  public CodeModuleDto toDto() {
    return new ClientCodeModuleDto(
      this.id,
      this.getDescription(),
      CodeModuleType.CLIENT,
      this.getInputs().stream().map(Input::toDto).toList(),
      this.getOutputs().stream().map(Output::toDto).toList(),
      this.getConfigs().stream().map(Config::toDto).toList()
    );
  }

  String translateOutputDeclarations(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    Collection<Output> outputs = this.getOutputs();
    if (outputs.size() == 0) {
      return "";
    }
    return outputs.stream().map(
      output -> output.translateDeclaration(symbolTable, depth, exportTarget)
    ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)));
  }

  String translateReturn(ExportTarget exportTarget) {
    Collection<Output> outputs = this.getOutputs();

    if (outputs.size() == 0) {
      return "";
    }

    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "return {%s};",
        outputs.stream().map(
          output -> String.format("%s_%s: %s", this.getId(), output.getId(), output.getId())
        ).collect(Collectors.joining(", "))
      );
    };
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    Collection<Output> outputs = this.getOutputs();
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "function %s(%s): %s {",
            CompilerUtils.buildLinePadding(depth + 2) + "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "}"
          },
          0
        ),
        this.getId(),
        this.getInputs().stream().map(
          input -> input.translate(symbolTable, depth, exportTarget)
        ).collect(Collectors.joining(", ")),
        (outputs.size() != 0)
          ? String.format(
            "{%s}",
            outputs.stream().map(
              output -> output.translateReturnType(symbolTable, depth, exportTarget, this.getId())
            ).collect(Collectors.joining(", "))
          )
          : "void",
        String.join(
          "\n" + CompilerUtils.buildLinePadding(depth + 2),
          List.of(
            this.translateOutputDeclarations(symbolTable, depth, exportTarget),
            this.body.getBodyComponents().stream().map(
              bodyComponent -> bodyComponent.translate(symbolTable, depth + 1, exportTarget)
            ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2))),
            this.translateReturn(exportTarget)
          )
        )
      );
    };
  }

  public void setHead(Head head) {
    this.head = head;
  }

  public void setBody(Body body) {
    this.body = body;
  }

  public Optional<Input> getInput(String id) {
    return Optional.ofNullable(this.head.inputs.get(id));
  }

  public Optional<Output> getOutput(String id) {
    return Optional.ofNullable(this.head.outputs.get(id));
  }

  public Collection<Input> getInputs() {
    return this.head.inputs.values();
  }

  public Collection<Output> getOutputs() {
    return this.head.outputs.values();
  }

  public Collection<Config> getConfigs() {
    return this.head.configs.values();
  }
}
