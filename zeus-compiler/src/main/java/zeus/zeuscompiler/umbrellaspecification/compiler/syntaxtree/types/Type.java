package zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.types;

public abstract class Type {
  public abstract boolean compatible(Type type);

  @Override
  public abstract boolean equals(Object obj);
}
