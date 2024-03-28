package zeus.zeuscompiler.thunder.dtos;

import java.util.List;

public class ClientCodeModuleDto extends CodeModuleDto {
  final List<PortDto> inputs;
  final List<PortDto> outputs;
  final List<ConfigDto> configs;

  public ClientCodeModuleDto(
    String id,
    String description,
    CodeModuleType codeModuleType,
    List<PortDto> inputs,
    List<PortDto> outputs,
    List<ConfigDto> configs
  ) {
    super(id, description, codeModuleType);
    this.inputs = inputs;
    this.outputs = outputs;
    this.configs = configs;
  }

  public List<PortDto> getInputs() {
    return inputs;
  }

  public List<PortDto> getOutputs() {
    return outputs;
  }

  public List<ConfigDto> getConfigs() {
    return configs;
  }
}
