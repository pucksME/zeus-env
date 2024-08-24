package zeus.zeuscompiler.services;

import zeus.zeuscompiler.CompilerError;

import java.util.ArrayList;
import java.util.List;

public class CompilerErrorService implements Service {
  private final List<CompilerError> compilerErrors;

  public CompilerErrorService() {
    this.compilerErrors = new ArrayList<>();
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
}
