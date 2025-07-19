package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.shared.message.payload.modelchecking.Location;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.DeclarationTypeStatement;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.dtos.ClientCodeModuleDto;
import zeus.zeuscompiler.thunder.dtos.CodeModuleDto;
import zeus.zeuscompiler.thunder.dtos.CodeModuleType;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

  public boolean hasEmptyTranslation() {
    return this.head.isEmpty() &&
      this.body.getBodyComponents().stream()
        .allMatch(
          bodyComponent -> bodyComponent instanceof DeclarationTypeStatement &&
            ((DeclarationTypeStatement) bodyComponent).isPublic()
        );
  }

  @Override
  public void checkTypes() {
    for (Input input : this.head.inputs.values()) {
      input.checkTypes();
    }

    for (Output output : this.head.outputs.values()) {
      output.checkTypes();
    }

    for (Config config : this.head.configs.values()) {
      config.checkTypes();
    }

    super.checkTypes();
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

  String translateOutputDeclarations(int depth, ExportTarget exportTarget) {
    Collection<Output> outputs = this.getOutputs();
    if (outputs.isEmpty()) {
      return "";
    }
    return outputs.stream().map(
      output -> output.translateDeclaration(depth, exportTarget)
    ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)));
  }

  String translateReturn(ExportTarget exportTarget) {
    Collection<Output> outputs = this.getOutputs();

    if (outputs.isEmpty()) {
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
  public String translate(int depth, ExportTarget exportTarget) {
    if (this.hasEmptyTranslation() || this instanceof RequestCodeModule || this instanceof ResponseCodeModule) {
      return "";
    }

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
          input -> input.translate(depth, exportTarget)
        ).collect(Collectors.joining(", ")),
        (!outputs.isEmpty())
          ? String.format(
            "{%s}",
            outputs.stream().map(
              output -> output.translateReturnType(depth, exportTarget, this.getId())
            ).collect(Collectors.joining(", "))
          )
          : "void",
        String.join(
          "\n" + CompilerUtils.buildLinePadding(depth + 2),
          List.of(
            this.translateOutputDeclarations(depth, exportTarget),
            this.body.getBodyComponents().stream().map(
              bodyComponent -> bodyComponent.translate(depth + 1, exportTarget)
            ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2))),
            this.translateReturn(exportTarget)
          )
        )
      );
    };
  }

  public Optional<ComponentSearchResult> searchComponent(Location location) {
    List<Component> components = this.getComponents();

    for (int i = 0; i < components.size(); i++) {
      Component component = components.get(i);
      Optional<ComponentSearchResult> componentOptional = component.searchComponent(location, i, new LinkedList<>());

      if (componentOptional.isPresent()) {
        return componentOptional;
      }
    }

    return Optional.empty();
  }

  public Optional<ComponentSearchResult> getFirstComponent() {
    if (this.getComponents().isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new ComponentSearchResult(this.getComponents().getFirst(), 0, new LinkedList<>()));
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

  public List<Component> getComponents() {
    return Stream.concat(
      this.head.getHeadComponents().stream(),
      this.body.getBodyComponents().stream()
    ).toList();
  }
}
