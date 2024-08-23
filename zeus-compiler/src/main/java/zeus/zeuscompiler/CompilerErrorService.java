package zeus.zeuscompiler;

import java.util.ArrayList;
import java.util.List;

public class CompilerErrorService implements Service {
  private List<CompilerError> compilerErrors;

  public CompilerErrorService() {
    this.compilerErrors = new ArrayList<>();
  }

  public void addError(CompilerError compilerError) {
    this.compilerErrors.add(compilerError);
  }
}
