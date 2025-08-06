package zeus.shared.message.payload;

import zeus.shared.message.payload.modelchecking.Path;

import java.util.List;
import java.util.Optional;

public class VerificationResponse {
  private List<VerificationResult> verificationResults;
  private boolean error;

  public VerificationResponse() {
    this.error = true;
  }

  public VerificationResponse(List<VerificationResult> verificationResults) {
    this();
    if (!verificationResults.isEmpty()) {
      this.verificationResults = verificationResults;
    }
  }

  public Optional<List<VerificationResult>> getVerificationResults() {
    return Optional.ofNullable(verificationResults);
  }
}
