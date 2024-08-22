package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.symboltable.VariableType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.InvalidAssignmentException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownVariableException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class AssignmentStatement extends Statement {
  String id;
  Expression assignExpression;

  public AssignmentStatement(int line, int linePosition, String id, Expression assignExpression) {
    super(line, linePosition);
    this.id = id;
    this.assignExpression = assignExpression;
  }

  @Override
  public void checkTypes(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors) {
    Optional<Type> assignTypeOptional = this.assignExpression.evaluateType(symbolTable, compilerErrors);

    if (assignTypeOptional.isEmpty()) {
      return;
    }

    Optional<VariableInformation> variableInformationOptional = symbolTable.getVariable(
      symbolTable.getCurrentCodeModule(),
      this.id,
      this.getLine(),
      this.getLinePosition()
    );

    if (variableInformationOptional.isEmpty()) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new UnknownVariableException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    VariableInformation variableInformation = variableInformationOptional.get();

    if (variableInformation.getVariableType() == VariableType.INPUT ||
      variableInformation.getVariableType() == VariableType.CONFIG) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new InvalidAssignmentException(),
        CompilerPhase.TYPE_CHECKER
      ));
      return;
    }

    if (!assignTypeOptional.get().compatible(symbolTable, compilerErrors, variableInformationOptional.get().getType())) {
      compilerErrors.add(new CompilerError(
        this.assignExpression.getLine(),
        this.assignExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(ClientSymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        "%s = %s;",
        this.id,
        this.assignExpression.translate(symbolTable, depth, exportTarget)
      );
    };
  }
}
