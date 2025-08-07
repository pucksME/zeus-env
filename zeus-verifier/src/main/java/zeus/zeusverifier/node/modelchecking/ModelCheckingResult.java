package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.Optional;

public class ModelCheckingResult {
  private Path path;
  private ModelCheckingResultStatus status;

  public ModelCheckingResult(Path path) {
    this.path = path;
    this.status = ModelCheckingResultStatus.OK;
  }

  public ModelCheckingResult(ModelCheckingResultStatus status) {
    this.status = status;
  }

  public Optional<Path> getPath() {
    return Optional.ofNullable(path);
  }

  public ModelCheckingResultStatus getStatus() {
    return status;
  }
}
