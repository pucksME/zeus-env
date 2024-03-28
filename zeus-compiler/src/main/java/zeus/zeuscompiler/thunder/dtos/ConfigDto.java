package zeus.zeuscompiler.thunder.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "configType",
  use = JsonTypeInfo.Id.NAME,
  visible = true
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = InputConfigDto.class, name = "InputConfigDto"),
  @JsonSubTypes.Type(value = SelectionConfigDto.class, name = "SelectionConfigDto")
})
public abstract class ConfigDto {
  final String id;
  final TypeDto type;
  final ConfigType configType;

  public ConfigDto(String id, TypeDto type, ConfigType configType) {
    this.id = id;
    this.type = type;
    this.configType = configType;
  }

  public String getId() {
    return id;
  }

  public TypeDto getType() {
    return type;
  }

  public ConfigType getConfigType() {
    return configType;
  }
}
