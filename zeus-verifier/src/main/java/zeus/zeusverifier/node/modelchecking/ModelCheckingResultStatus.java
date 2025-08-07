package zeus.zeusverifier.node.modelchecking;

public enum ModelCheckingResultStatus {
  OK,
  NO_COUNTEREXAMPLE_FOUND,
  INFEASIBLE_PREDICATE_VALUATIONS,
  ABSTRACTION_FAILED,
  UNSUPPORTED_COMPONENT
}
