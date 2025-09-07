package zeus.shared.message.payload.modelchecking;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class Valuation {
  boolean value;
  final String className;

  public Valuation(boolean value) {
    this.className = this.getClass().getName();
    this.value = value;
  }

  public static Set<Valuation> filter(List<Valuation> valuations, Set<Integer> unsatisfiableCore) {
    return IntStream.range(0, unsatisfiableCore.size()).mapToObj(valuations::get).collect(Collectors.toSet());
  }
}
