package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Path {
  private final List<State> states;

  public Path(List<State> states) {
    this.states = states;
  }

  public Set<Predicate> getPredicates() {
    return (this.states.isEmpty())
      ? new HashSet<>()
      : this.states.getLast().getPredicates().orElse(new HashSet<>());
  }

  public List<State> getStates() {
    return this.states;
  }

  @Override
  public String toString() {
    return IntStream.range(0, this.states.size())
      .mapToObj(i -> String.format("%s) %s", i + 1, this.states.get(i)))
      .collect(Collectors.joining("\n"));
  }
}
