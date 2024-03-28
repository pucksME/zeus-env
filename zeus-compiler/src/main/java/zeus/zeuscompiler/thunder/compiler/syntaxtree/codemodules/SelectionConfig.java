package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.InvalidConfigException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.MissingDeclarationException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.ListExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralExpression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ListType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.thunder.dtos.ConfigDto;
import zeus.zeuscompiler.thunder.dtos.ConfigType;
import zeus.zeuscompiler.thunder.dtos.SelectionConfigDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SelectionConfig extends Config {
  public SelectionConfig(int line, int linePosition, String id, Type type, Expression declarationExpression) {
    super(line, linePosition, id, type, declarationExpression);
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (this.declarationExpression == null) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new MissingDeclarationException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    Optional<Type> declarationTypeOptional = this.declarationExpression.evaluateType(symbolTable, compilerErrors);

    if (declarationTypeOptional.isEmpty()) {
      return;
    }

    if (!declarationTypeOptional.get().compatible(symbolTable, compilerErrors, this.type)) {
      compilerErrors.add(new CompilerError(
        this.declarationExpression.getLine(),
        this.declarationExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (this.type instanceof ListType &&
      ((ListType) this.type).getType() instanceof PrimitiveType &&
      this.declarationExpression instanceof ListExpression &&
      ((ListExpression) this.declarationExpression).getExpressions().stream().allMatch(
        expression -> expression instanceof LiteralExpression
      )) {
      return;
    }

    compilerErrors.add(new CompilerError(
      this.getLine(),
      this.getLinePosition(),
      new InvalidConfigException(),
      CompilerPhase.TYPE_CHECKER
    ));
  }

  @Override
  public ConfigDto toDto() {
    Map<String, String> options = new HashMap<>();
    if (this.declarationExpression != null && this.type instanceof ListType) {
      options = ((ListExpression) this.declarationExpression).getExpressions().stream().collect(Collectors.toMap(
        expression -> ((LiteralExpression) expression).getValue(),
        expression -> ((LiteralExpression) expression).getValue()
      ));
    }

    return new SelectionConfigDto(this.getId(), this.type.toDto(), ConfigType.SELECTION, options);
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
