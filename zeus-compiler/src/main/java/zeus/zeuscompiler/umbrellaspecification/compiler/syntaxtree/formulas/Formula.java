package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.formulas;

import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Node;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types.Type;

import java.util.Optional;

public abstract class Formula extends Node {
  public Formula(int line, int linePosition) {
    super(line, linePosition);
  }

  public abstract Optional<Type> evaluateType();
}