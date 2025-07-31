package zeus.zeuscompiler.services;

import zeus.zeuscompiler.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class CompilerErrorService implements Service {
  private final List<CompilerError> compilerErrors;
  private int lineOffset;

  public CompilerErrorService() {
    this.compilerErrors = new ArrayList<>();
    this.lineOffset = 0;
  }

  public void addError(CompilerError compilerError) {
    this.compilerErrors.add(compilerError);
  }

  public boolean hasErrors() {
    return !this.compilerErrors.isEmpty();
  }

  @Override
  public void reset() {
    this.compilerErrors.clear();
  }

  public void setLineOffset(int lineOffset) {
    this.lineOffset = lineOffset;
  }

  public void resetLineOffset() {
    this.lineOffset = 0;
  }

  public int getLineOffset() {
    return lineOffset;
  }

  public List<CompilerError> getErrors() {
    return compilerErrors;
  }
}
