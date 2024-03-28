package zeus.zeuscompiler;

import zeus.zeuscompiler.thunder.compiler.syntaxtree.Convertable;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.thunder.compiler.utils.ThunderUtils;
import zeus.zeuscompiler.thunder.dtos.ErrorDto;

public class CompilerError implements Convertable<ErrorDto> {
  final int line;
  final int linePosition;
  Exception exception;
  final CompilerPhase compilerPhase;

  public CompilerError(int line, int linePosition, Exception exception, CompilerPhase compilerPhase) {
    this.line = line;
    this.linePosition = linePosition;
    this.exception = exception;
    this.compilerPhase = compilerPhase;
  }

  public String getMessage() {
    if (this.exception == null) {
      return ThunderUtils.buildErrorMessage("Error", this.line, this.linePosition);
    }

    return ThunderUtils.buildErrorMessage(this.exception.toString(), this.line, this.linePosition);
  }

  @Override
  public ErrorDto toDto() {
    return new ErrorDto(this.line, this.linePosition, this.getMessage());
  }

  public int getLine() {
    return line;
  }

  public int getLinePosition() {
    return linePosition;
  }

  public Exception getException() {
    return exception;
  }

  public CompilerPhase getCompilerPhase() {
    return compilerPhase;
  }
}
