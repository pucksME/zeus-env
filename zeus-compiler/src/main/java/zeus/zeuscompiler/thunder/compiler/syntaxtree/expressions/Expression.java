package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import zeus.shared.formula.Formula;
import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.TypeCheckableNode;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

import java.util.Map;
import java.util.Optional;

public abstract class Expression extends TypeCheckableNode implements Translatable {
  public Expression(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract Optional<Type> evaluateType();

  public abstract Formula toFormula(Map<String, VariableInformation> variables);
}
