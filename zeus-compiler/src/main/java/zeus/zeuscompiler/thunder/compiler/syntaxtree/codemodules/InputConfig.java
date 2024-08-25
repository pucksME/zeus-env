package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.InvalidConfigException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.thunder.dtos.ConfigDto;
import zeus.zeuscompiler.thunder.dtos.ConfigType;
import zeus.zeuscompiler.thunder.dtos.InputConfigDto;

import java.util.List;

public class InputConfig extends Config {
  public InputConfig(int line, int linePosition, String id, Type type, Expression declarationExpression) {
    super(line, linePosition, id, type, declarationExpression);
  }

  @Override
  public void checkTypes() {
    if (!(this.type instanceof PrimitiveType) || declarationExpression != null) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new InvalidConfigException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    this.type.checkType();
  }

  @Override
  public ConfigDto toDto() {
    return new InputConfigDto(this.getId(), this.type.toDto(), ConfigType.INPUT);
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return "";
  }
}
