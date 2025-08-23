package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.Optional;

public class CounterexampleAnalysisResult {
  private final Path path;
  private Path validPath;

  public CounterexampleAnalysisResult(Path path) {
    this.path = path;
  }

  public CounterexampleAnalysisResult(Path path, Path validPath) {
    this(path);
    this.validPath = validPath;
  }

  public Path getPath() {
    return path;
  }

  public Optional<Path> getValidPath() {
    return Optional.ofNullable(validPath);
  }
}
