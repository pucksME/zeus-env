package zeus.zeusverifier.node.counterexampleanalysis;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import zeus.shared.formula.Formula;
import zeus.shared.formula.unary.NotFormula;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.counterexampleanalysis.CounterexampleAnalysisFailed;
import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.message.payload.modelchecking.State;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Component;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.AssertStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.IfStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.WhileStatement;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;

import java.util.*;

public class CounterexampleAnalyzer {
  Path path;
  Map<UUID, Predicate> predicates;
  ClientCodeModule clientCodeModule;
  CounterexampleAnalysisNode counterexampleAnalysisNode;

  public CounterexampleAnalyzer(
    Path path,
    Map<UUID, Predicate> predicates,
    ClientCodeModule clientCodeModule,
    CounterexampleAnalysisNode counterexampleAnalysisNode
  ) {
    this.path = path;
    this.predicates = predicates;
    this.clientCodeModule = clientCodeModule;
    this.counterexampleAnalysisNode = counterexampleAnalysisNode;
  }

  private Optional<List<Component>> findComponents(Path path) {
    List<Component> components = new ArrayList<>();

    for (State state : path.states()) {
      Location location = state.location();
      Optional<ComponentSearchResult> componentSearchResultOptional = this.clientCodeModule.searchComponent(location);

      if (componentSearchResultOptional.isEmpty()) {
        this.counterexampleAnalysisNode.sendMessage(new Message<>(
          new CounterexampleAnalysisFailed(
            this.counterexampleAnalysisNode.getUuid(),
            String.format("could not find component at location %s:%s", location.line(), location.linePosition())
          ),
          new Recipient(NodeType.ROOT)
        ));
        return Optional.empty();
      }

      components.add(componentSearchResultOptional.get().getComponent());
    }

    return Optional.of(components);
  }

  private Optional<List<Formula>> findNewPredicateCandidates(
    List<Formula> formulas,
    CounterexampleAnalysisHistory counterexampleAnalysisHistory
  ) {
    try (Context context = new Context()) {
      Solver solver = context.mkSolver();
      for (int j = 0; j < formulas.size(); j++) {
        Formula formula = formulas.get(j);
        solver.assertAndTrack(formula.toFormula(context), context.mkBoolConst(String.valueOf(j)));
      }

      if (solver.check() == Status.SATISFIABLE) {
        counterexampleAnalysisHistory.addFormulas(formulas);
        return Optional.of(new ArrayList<>());
      }

      for (Expr expr : solver.getUnsatCore()) {
        String id = expr.getFuncDecl().getName().toString();
        try {
          return Optional.of(new ArrayList<>(counterexampleAnalysisHistory.getFormulaHistory(Integer.parseInt(id))));
        } catch (NumberFormatException numberFormatException) {
          this.counterexampleAnalysisNode.sendMessage(new Message<>(new CounterexampleAnalysisFailed(
            this.counterexampleAnalysisNode.getUuid(),
            String.format("invalid unsatisfiable core id \"%s\"", id)
          )));
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }

  public Optional<Path> analyze() {
    Optional<Map<String, VariableInformation>> variablesOptional = this.clientCodeModule.getVariables();

    if (variablesOptional.isEmpty()) {
      this.counterexampleAnalysisNode.sendMessage(new Message<>(
        new CounterexampleAnalysisFailed(
          this.counterexampleAnalysisNode.getUuid(),
          "code module variable information not present"
        ),
        new Recipient(NodeType.ROOT)
      ));
      return Optional.empty();
    }

    Map<String, VariableInformation> variables = variablesOptional.get();
    Optional<List<Component>> componentsOptional = this.findComponents(path);

    if (componentsOptional.isEmpty()) {
      return Optional.empty();
    }

    List<Component> components = componentsOptional.get();
    CounterexampleAnalysisHistory counterexampleAnalysisHistory = new CounterexampleAnalysisHistory();
    List<Formula> newPredicateCandidates = new ArrayList<>();
    Path counterexample = new Path(new ArrayList<>());

    for (int i = components.size() - 1; i >= 0; i--) {
      Component component = components.get(i);
      counterexample.states().addFirst(new State(new Location(component.getLine(), component.getLinePosition())));
      List<Formula> formulas = counterexampleAnalysisHistory.getCurrentFormulas();

      switch (component) {
        case IfStatement ifStatement -> formulas.add((i == components.size() - 1 ||
          !components.get(i + 1).equals(ifStatement.getThenBody().getBodyComponents().getFirst()))
            ? new NotFormula(ifStatement.getConditionExpression().toFormula(variables))
            : ifStatement.getConditionExpression().toFormula(variables));
        case WhileStatement whileStatement -> formulas.add((i == components.size() - 1 ||
          !components.get(i + 1).equals(whileStatement.getBody().getBodyComponents().getFirst()))
            ? new NotFormula(whileStatement.getConditionExpression().toFormula(variables))
            : whileStatement.getConditionExpression().toFormula(variables));
        case AssertStatement assertStatement -> formulas.add((i == components.size() - 1)
          ? new NotFormula(assertStatement.getExpression().toFormula(variables))
          : assertStatement.getExpression().toFormula(variables));
        default -> {
          this.counterexampleAnalysisNode.sendMessage(new Message<>(new CounterexampleAnalysisFailed(
            this.counterexampleAnalysisNode.getUuid(),
            String.format("unsupported component \"%s\"", component.getClass().getSimpleName())
          )));
          return Optional.empty();
        }
      }

      Optional<List<Formula>> newPredicateCandidatesOptional = this.findNewPredicateCandidates(
        formulas,
        counterexampleAnalysisHistory
      );

      if (newPredicateCandidatesOptional.isEmpty()) {
        return Optional.empty();
      }

      newPredicateCandidates.addAll(newPredicateCandidatesOptional.get());

      if (!newPredicateCandidates.isEmpty()) {
        break;
      }
    }

    if (newPredicateCandidates.isEmpty()) {
      return Optional.of(counterexample);
    }

    // TODO: handle new predicate candidates
    return Optional.empty();
  }
}
