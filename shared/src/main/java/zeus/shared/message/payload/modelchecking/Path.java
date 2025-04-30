package zeus.shared.message.payload.modelchecking;

import zeus.shared.predicate.Predicate;

import java.util.List;
import java.util.Set;

public record Path(List<StatementLocation> statements, Set<Predicate> predicates) {
}
