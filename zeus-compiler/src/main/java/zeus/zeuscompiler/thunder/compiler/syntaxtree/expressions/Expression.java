package zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;

import java.util.Optional;

public abstract class Expression extends BodyComponent {
  public Expression(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract Optional<Type> evaluateType();

  public abstract Expr toFormula(Context context);
}
