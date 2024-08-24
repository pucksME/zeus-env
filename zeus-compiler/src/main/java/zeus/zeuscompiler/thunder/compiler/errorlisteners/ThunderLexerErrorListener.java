package zeus.zeuscompiler.thunder.compiler.errorlisteners;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;

import java.util.List;

public class ThunderLexerErrorListener extends BaseErrorListener {

  @Override
  public void syntaxError(
    Recognizer<?, ?> recognizer,
    Object offendingSymbol,
    int line,
    int charPositionInLine,
    String msg,
    RecognitionException e
  ) {
    ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
      line,
      charPositionInLine,
      e,
      CompilerPhase.LEXER
    ));
  }
}
