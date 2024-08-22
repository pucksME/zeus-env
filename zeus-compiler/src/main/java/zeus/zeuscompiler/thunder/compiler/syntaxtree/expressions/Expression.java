package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;

import java.util.List;
import java.util.Optional;

public abstract class Expression extends BodyComponent {
  public Expression(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract Optional<Type> evaluateType(ClientSymbolTable symbolTable, List<CompilerError> compilerErrors);
}
