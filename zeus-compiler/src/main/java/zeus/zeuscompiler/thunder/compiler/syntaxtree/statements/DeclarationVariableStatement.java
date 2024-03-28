package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class DeclarationVariableStatement extends Statement {
  String id;
  Type type;
  // null for variables without initial assignment
  Expression declarationExpression;

  public DeclarationVariableStatement(
    int line,
    int linePosition,
    String id,
    Type type,
    Expression declarationExpression
  ) {
    super(line, linePosition);
    this.id = id;
    this.type = type;
    this.declarationExpression = declarationExpression;
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (this.declarationExpression == null) {
      this.type.checkType(symbolTable, compilerErrors);
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
    }
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "let %s: %s%s;",
        this.id,
        this.type.translate(symbolTable, depth, exportTarget),
        (this.declarationExpression != null)
          ? String.format(" = %s", this.declarationExpression.translate(symbolTable, depth, exportTarget))
          : ""
      );
    };
  }
}
