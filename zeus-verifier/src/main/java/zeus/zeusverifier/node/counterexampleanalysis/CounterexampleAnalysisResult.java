package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.payload.counterexampleanalysis.VariableAssignment;
import zeus.shared.message.payload.modelchecking.Path;

import java.util.Optional;
import java.util.Set;

public class CounterexampleAnalysisResult {
  private final Path path;
  private Path pivotPath;
  private Set<VariableAssignment> variableAssignments;

  public CounterexampleAnalysisResult(Path path) {
    this.path = path;
  }

  public CounterexampleAnalysisResult(Path path, Set<VariableAssignment> variableAssignments) {
    this.path = path;
    this.variableAssignments = variableAssignments;
  }

  public CounterexampleAnalysisResult(Path path, Path pivotPath) {
    this(path);
    this.pivotPath = pivotPath;
  }

  public Path getPath() {
    return path;
  }

  public Optional<Path> getPivotPath() {
    return Optional.ofNullable(pivotPath);
  }

  public Optional<Set<VariableAssignment>> getVariableAssignments() {
    return Optional.ofNullable(variableAssignments);
  }
}
