package zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.thunder.dtos.ConfigDto;

public abstract class Config extends HeadComponent implements Convertable<ConfigDto> {
  // null for configs without initial assignment
  Expression declarationExpression;

  public Config(int line, int linePosition, String id, Type type, Expression declarationExpression) {
    super(line, linePosition, id, type);
    this.declarationExpression = declarationExpression;
  }
}
