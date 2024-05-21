package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.thunder.compiler.symboltable.SymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Input;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Output;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownCodeModuleException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.UnknownCodeModulePortException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.port.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.ObjectType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;
import java.util.Optional;

public class ConnectionStatement extends Statement {
  CodeModuleInputExpression codeModuleInputExpression;
  CodeModuleOutputExpression codeModuleOutputExpression;

  public ConnectionStatement(
    int line,
    int linePosition,
    CodeModuleInputExpression codeModuleInputExpression,
    CodeModuleOutputExpression codeModuleOutputExpression
  ) {
    super(line, linePosition);
    this.codeModuleInputExpression = codeModuleInputExpression;
    this.codeModuleOutputExpression = codeModuleOutputExpression;
  }

  private void addInferredType(
    SymbolTable symbolTable,
    List<CompilerError> compilerErrors,
    ObjectType objectType,
    String inferredPropertyId,
    Type inferredPropertyType
  ) {
    Optional<Type> propertyTypeOptional = objectType.getPropertyType(inferredPropertyId);

    if (propertyTypeOptional.isEmpty()) {
      objectType.addPropertyType(inferredPropertyId, inferredPropertyType);
      return;
    }

    if (!propertyTypeOptional.get().compatible(symbolTable, compilerErrors, inferredPropertyType)) {
      compilerErrors.add(new CompilerError(
        this.codeModuleOutputExpression.getLine(),
        this.codeModuleOutputExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  // TODO: handle cases where request and response is connected directly, e.q. request.url.param -> response.body.result
  private void inferRouteTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    if (this.codeModuleOutputExpression instanceof CodeModuleRequestExpression) {
      Optional<CodeModule> codeModuleOptional = symbolTable.getCodeModules().getCodeModule(
        this.codeModuleOutputExpression.getCodeModuleId()
      );

      if (codeModuleOptional.isEmpty() || !(codeModuleOptional.get() instanceof ClientCodeModule)) {
        compilerErrors.add(new CompilerError(
          this.codeModuleOutputExpression.getLine(),
          this.codeModuleOutputExpression.getLinePosition(),
          new UnknownCodeModuleException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return;
      }

      Optional<CodeModule> inputCodeModuleOptional = symbolTable.getCodeModules().getCodeModule(
        this.codeModuleInputExpression.getCodeModuleId()
      );

      if (inputCodeModuleOptional.isEmpty() || !(inputCodeModuleOptional.get() instanceof ClientCodeModule)) {
        compilerErrors.add(new CompilerError(
          this.codeModuleInputExpression.getLine(),
          this.codeModuleInputExpression.getLinePosition(),
          new UnknownCodeModuleException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return;
      }

      Optional<Input> inputOptional = ((ClientCodeModule) inputCodeModuleOptional.get()).getInput(
        this.codeModuleInputExpression.getInputId()
      );

      if (inputOptional.isEmpty()) {
        compilerErrors.add(new CompilerError(
          this.codeModuleInputExpression.getLine(),
          this.codeModuleInputExpression.getLinePosition(),
          new UnknownCodeModulePortException(),
          CompilerPhase.TYPE_CHECKER
        ));
        return;
      }

      Input input = inputOptional.get();

      if (((CodeModuleRequestExpression) this.codeModuleOutputExpression).getRequestParameterType() == RequestParameterType.URL) {
        Optional<Output> outputOptional = ((ClientCodeModule) codeModuleOptional.get()).getOutput("url");

        if (outputOptional.isEmpty() || !(outputOptional.get().getType() instanceof ObjectType)) {
          compilerErrors.add(new CompilerError(
            this.codeModuleOutputExpression.getLine(),
            this.codeModuleOutputExpression.getLinePosition(),
            new UnknownCodeModulePortException(),
            CompilerPhase.TYPE_CHECKER
          ));
          return;
        }

        this.addInferredType(
          symbolTable,
          compilerErrors,
          (ObjectType) outputOptional.get().getType(),
          input.getId(),
          input.getType()
        );
        return;
      }
      // TODO: type inference for request body
      return;
    }

    if (codeModuleInputExpression instanceof CodeModuleResponseExpression) {
      // TODO: type inference for responses
    }
  }

  @Override
  public void checkTypes(SymbolTable symbolTable, List<CompilerError> compilerErrors) {
    this.inferRouteTypes(symbolTable, compilerErrors);
    if (codeModuleInputExpression instanceof CodeModuleResponseExpression) {
      return;
    }

    Optional<Type> codeModuleInputTypeOptional = this.codeModuleInputExpression.evaluateType(
      symbolTable,
      compilerErrors
    );

    if (codeModuleInputTypeOptional.isEmpty()) {
      return;
    }

    if (codeModuleOutputExpression instanceof CodeModuleRequestExpression) {
      return;
    }

    Optional<Type> codeModuleOutputTypeOptional = this.codeModuleOutputExpression.evaluateType(
      symbolTable,
      compilerErrors
    );

    if (codeModuleOutputTypeOptional.isEmpty()) {
      return;
    }

    if (!codeModuleInputTypeOptional.get().compatible(
      symbolTable,
      compilerErrors,
      codeModuleOutputTypeOptional.get()
    )) {
      compilerErrors.add(new CompilerError(
        this.getLine(),
        this.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public String translate(SymbolTable symbolTable, int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> "";
    };
  }

  public CodeModuleInputExpression getCodeModuleInputExpression() {
    return codeModuleInputExpression;
  }

  public CodeModuleOutputExpression getCodeModuleOutputExpression() {
    return codeModuleOutputExpression;
  }
}
