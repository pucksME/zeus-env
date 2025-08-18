package zeus.shared.message.payload.modelchecking;

public enum StopModelCheckingTaskRequestStatus {
  VALID_COUNTEREXAMPLE,
  INVALID_COUNTEREXAMPLE,
  NO_NEW_PREDICATES,
  NO_COUNTEREXAMPLE_FOUND,
  INFEASIBLE_PATH
}
