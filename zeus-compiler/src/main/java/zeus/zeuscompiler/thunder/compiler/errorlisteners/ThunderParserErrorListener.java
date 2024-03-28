package zeus.zeuscompiler.thunder.compiler.errorlisteners;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;

public class ThunderParserErrorListener extends BaseErrorListener {

  List<CompilerError> compilerErrors;

  public ThunderParserErrorListener(List<CompilerError> compilerErrors) {
    this.compilerErrors = compilerErrors;
  }

  @Override
  public void syntaxError(
    Recognizer<?, ?> recognizer,
    Object offendingSymbol,
    int line,
    int charPositionInLine,
    String msg,
    RecognitionException e
  ) {
    this.compilerErrors.add(new CompilerError(line, charPositionInLine, e, CompilerPhase.PARSER));
  }
}
