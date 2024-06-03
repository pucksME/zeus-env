package zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking;

import zeus.zeuscompiler.thunder.compiler.utils.ThunderUtils;

public class UnsupportedCodeModuleComponentsException extends TypeCheckingException {
  String codeModuleId;
  CodeModuleComponent codeModuleComponent;

  public UnsupportedCodeModuleComponentsException(String codeModuleId, CodeModuleComponent codeModuleComponent) {
    this.codeModuleId = codeModuleId;
    this.codeModuleComponent = codeModuleComponent;
  }

  public String getCodeModuleId() {
    return codeModuleId;
  }

  public CodeModuleComponent getCodeModuleComponent() {
    return codeModuleComponent;
  }

  @Override
  public String toString() {
    return String.format(
      "Code module \"%s\" does not support %s components",
      this.codeModuleId,
      ThunderUtils.codeModuleComponentToString(this.codeModuleComponent)
    );
  }
}
