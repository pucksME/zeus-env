package zeus.zeuscompiler.thunder.dtos;

import java.util.Map;

public class SelectionConfigDto extends ConfigDto {
  final Map<String, String> options;

  public SelectionConfigDto(String id, TypeDto type, ConfigType configType, Map<String, String> options) {
    super(id, type, configType);
    this.options = options;
  }

  public Map<String, String> getOptions() {
    return options;
  }
}
