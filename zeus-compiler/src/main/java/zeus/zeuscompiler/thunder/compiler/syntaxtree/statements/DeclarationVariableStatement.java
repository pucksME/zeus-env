package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
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
  public void checkTypes() {
    if (this.declarationExpression == null) {
      this.type.checkType();
      return;
    }

    Optional<Type> declarationTypeOptional = this.declarationExpression.evaluateType();

    if (declarationTypeOptional.isEmpty()) {
      return;
    }

    if (!declarationTypeOptional.get().compatible(this.type)) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.declarationExpression.getLine(),
        this.declarationExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "let %s: %s%s;",
        this.id,
        this.type.translate(depth, exportTarget),
        (this.declarationExpression != null)
          ? String.format(" = %s", this.declarationExpression.translate(depth, exportTarget))
          : ""
      );
    };
  }

  public String getId() {
    return id;
  }

  public Optional<Expression> getDeclarationExpression() {
    return Optional.ofNullable(declarationExpression);
  }
}
