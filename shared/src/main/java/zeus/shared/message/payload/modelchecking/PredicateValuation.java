package zeus.shared.message.payload.modelchecking;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class PredicateValuation {
  UUID predicateUuid;
  boolean value;

  public PredicateValuation(UUID predicateUuid, boolean value) {
    this.predicateUuid = predicateUuid;
    this.value = value;
  }

  public static List<Map<UUID, PredicateValuation>> getCombinations(
    Map<UUID, PredicateValuation> deterministicPredicateValuations,
    List<UUID> nonDeterministicPredicateValuations
  ) {
    return (nonDeterministicPredicateValuations.isEmpty())
      ? new ArrayList<>()
      : LongStream.range(0, (long) Math.pow(2, nonDeterministicPredicateValuations.size())).mapToObj(i -> {
          String valuation = Long.toBinaryString(i);
          String valuationBits = "0".repeat(
            nonDeterministicPredicateValuations.size() - valuation.length()
          ) + valuation;

          return Stream.concat(
            deterministicPredicateValuations.entrySet().stream(),
            IntStream.range(0, nonDeterministicPredicateValuations.size())
              .mapToObj(index -> Map.entry(
                nonDeterministicPredicateValuations.get(index),
                new PredicateValuation(
                  nonDeterministicPredicateValuations.get(index),
                  valuationBits.charAt(index) == '1'
                )
              ))
          ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }).toList();
  }

  public static List<Map<UUID, PredicateValuation>> getCombinations(List<UUID> nonDeterministicPredicateValuations) {
    return PredicateValuation.getCombinations(new HashMap<>(), nonDeterministicPredicateValuations);
  }

  @Override
  public int hashCode() {
    return this.predicateUuid.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return (obj instanceof PredicateValuation) && this.predicateUuid.equals(((PredicateValuation) obj).predicateUuid);
  }

  public void setValue(boolean value) {
    this.value = value;
  }

  public boolean getValue() {
    return value;
  }
}
