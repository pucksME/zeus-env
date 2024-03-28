package zeus.zeuscompiler.thunder.dtos;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
  include = JsonTypeInfo.As.EXISTING_PROPERTY,
  property = "codeModuleType",
  use = JsonTypeInfo.Id.NAME,
  visible = true
)
@JsonSubTypes({
  @JsonSubTypes.Type(value = ClientCodeModuleDto.class, name = "ClientCodeModuleDto"),
  @JsonSubTypes.Type(value = InstanceCodeModuleDto.class, name = "InstanceCodeModuleDto")
})
public abstract class CodeModuleDto {
  final String id;
  final String description;
  final CodeModuleType codeModuleType;

  public CodeModuleDto(String id, String description, CodeModuleType codeModuleType) {
    this.id = id;
    this.description = description;
    this.codeModuleType = codeModuleType;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public CodeModuleType getCodeModuleType() {
    return codeModuleType;
  }
}
