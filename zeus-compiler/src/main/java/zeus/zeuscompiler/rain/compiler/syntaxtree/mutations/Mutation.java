package zeus.zeuscompiler.rain.compiler.syntaxtree.mutations;

import zeus.zeuscompiler.Translatable;
import zeus.zeuscompiler.rain.compiler.syntaxtree.positions.Position;

public abstract class Mutation implements Translatable {
  Position position;

  public Mutation(Position position) {
    this.position = position;
  }
}
