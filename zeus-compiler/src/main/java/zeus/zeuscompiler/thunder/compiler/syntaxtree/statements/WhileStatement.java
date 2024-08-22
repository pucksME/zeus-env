package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Body;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class WhileStatement extends Statement {
  Expression conditionExpression;
  Body body;

  public WhileStatement(int line, int linePosition, Expression conditionExpression, Body body) {
    super(line, linePosition);
    this.conditionExpression = conditionExpression;
    this.body = body;
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> conditionTypeOptional = this.conditionExpression.evaluateType(symbolTable, compilerErrors);

    if (conditionTypeOptional.isEmpty()) {
      return;
    }

    Type conditionType = conditionTypeOptional.get();

    if (!(conditionType instanceof PrimitiveType) || ((PrimitiveType) conditionType).getType() != LiteralType.BOOLEAN) {
      compilerErrors.add(new CompilerError(
        this.conditionExpression.getLine(),
        this.conditionExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    for (BodyComponent bodyComponent : this.body.getBodyComponents()) {
      bodyComponent.checkTypes(symbolTable, compilerErrors);
    }
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "while (%s) {",
            CompilerUtils.buildLinePadding(depth + 2) + "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "}"
          },
          0
        ),
        this.conditionExpression.translate(symbolTable, depth, exportTarget),
        this.body.getBodyComponents().stream().map(
          bodyComponent -> bodyComponent.translate(symbolTable, depth + 1, exportTarget)
        ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)))
      );
    };
  }
}
