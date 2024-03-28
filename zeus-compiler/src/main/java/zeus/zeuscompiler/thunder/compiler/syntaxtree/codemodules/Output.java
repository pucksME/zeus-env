package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.DeclarationVariableStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.dtos.PortDto;

import java.util.List;

public class Output extends HeadComponent implements Convertable<PortDto> {
  // null for outputs without initial assignment
  Expression declarationExpression;

  public Output(int line, int linePosition, String id, Type type, Expression declarationExpression) {
    super(line, linePosition, id, type);
    this.declarationExpression = declarationExpression;
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    DeclarationVariableStatement declarationVariableStatement = new DeclarationVariableStatement(
      this.getLine(),
      this.getLinePosition(),
      this.id,
      this.type,
      this.declarationExpression
    );

    declarationVariableStatement.checkTypes(symbolTable, compilerErrors);
  }

  @Override
  public PortDto toDto() {
    return new PortDto(this.id, this.getType().toDto());
  }

  public String translateDeclaration(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return new DeclarationVariableStatement(
      -1,
      -1,
      this.getId(),
      this.getType(),
      this.declarationExpression
    ).translate(symbolTable, depth, exportTarget);
  }

  public String translateReturnType(SymbolTable symbolTable, int depth, ExportTarget exportTarget, String codeModuleId) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s_%s: %s",
        codeModuleId,
        this.id,
        this.type.translate(symbolTable, depth, exportTarget)
      );
    };
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return "";
  }
}
