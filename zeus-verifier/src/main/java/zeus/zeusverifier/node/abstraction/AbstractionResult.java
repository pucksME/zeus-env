package zeus.zeusverifier.node.abstraction;

import zeus.shared.message.payload.abstraction.AbstractLiteral;

import java.util.Optional;

public class AbstractionResult {
  private AbstractLiteral abstractLiteral;
  private final AbstractionResultStatus status;

  public AbstractionResult(AbstractionResultStatus status) {
    this.status = status;
    this.abstractLiteral = null;
  }

  public AbstractionResult(AbstractLiteral abstractLiteral) {
    this(AbstractionResultStatus.OK);
    this.abstractLiteral = abstractLiteral;
  }

  public Optional<AbstractLiteral> getAbstractLiteral() {
    return Optional.ofNullable(abstractLiteral);
  }

  public AbstractionResultStatus getStatus() {
    return status;
  }
}
