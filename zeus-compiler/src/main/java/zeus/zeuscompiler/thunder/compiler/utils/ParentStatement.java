package zeus.zeuscompiler.thunder.compiler.utils;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.Statement;

public class ParentStatement {
  Statement statement;
  int index;

  public ParentStatement(Statement statement, int index) {
    this.statement = statement;
    this.index = index;
  }

  public Statement getStatement() {
    return statement;
  }

  public int getIndex() {
    return index;
  }
}
