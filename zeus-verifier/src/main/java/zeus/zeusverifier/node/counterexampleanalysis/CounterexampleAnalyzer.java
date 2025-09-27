package zeus.zeusverifier.node.counterexampleanalysis;

import com.microsoft.z3.*;
import zeus.shared.formula.Formula;
import zeus.shared.formula.unary.NotFormula;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.counterexampleanalysis.CounterexampleAnalysisFailed;
import zeus.shared.message.payload.counterexampleanalysis.VariableAssignment;
import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.message.payload.modelchecking.State;
import zeus.shared.predicate.Predicate;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Component;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Input;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.*;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CounterexampleAnalyzer {
  private final UUID verificationUuid;
  Path path;
  Set<VariableAssignment> variableAssignments;
  Set<Predicate> predicates;
  ClientCodeModule clientCodeModule;
  CounterexampleAnalysisNode counterexampleAnalysisNode;

  public CounterexampleAnalyzer(
    UUID verificationUuid,
    Path path,
    ClientCodeModule clientCodeModule,
    CounterexampleAnalysisNode counterexampleAnalysisNode
  ) {
    this.verificationUuid = verificationUuid;
    this.path = path;
    this.variableAssignments = new HashSet<>();
    this.predicates = this.path.getPredicates();
    this.clientCodeModule = clientCodeModule;
    this.counterexampleAnalysisNode = counterexampleAnalysisNode;
  }

  private Optional<List<Component>> findComponents(Path path) {
    List<Component> components = new ArrayList<>();

    for (State state : path.getStates()) {
      Location location = state.getLocation();
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

  private void updateModel(Model model) {
    Optional<Map<String, VariableInformation>> variablesOptional = this.clientCodeModule.getVariables();
    if (variablesOptional.isEmpty()) {
      return;
    }

    this.variableAssignments = new HashSet<>();

    for (FuncDecl<?> funcDecl : model.getConstDecls()) {
      String variableName = funcDecl.getName().toString();
      if (!variablesOptional.get().containsKey(variableName)) {
        continue;
      }

      this.variableAssignments.add(new VariableAssignment(variableName, model.getConstInterp(funcDecl).toString()));
    }
  }

  private Optional<Set<Formula>> findNewPredicateCandidates(
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
        this.updateModel(solver.getModel());
        return Optional.of(new HashSet<>());
      }

      for (Expr expr : solver.getUnsatCore()) {
        String id = expr.getFuncDecl().getName().toString();
        try {
          return Optional.of(counterexampleAnalysisHistory.getFormulaHistory(Integer.parseInt(id)).stream()
            .flatMap(formula -> formula.extractPredicateFormulas().stream())
            .collect(Collectors.toSet()));
        } catch (NumberFormatException numberFormatException) {
          this.counterexampleAnalysisNode.sendMessage(new Message<>(new CounterexampleAnalysisFailed(
            this.counterexampleAnalysisNode.getUuid(),
            String.format("invalid unsatisfiable core id \"%s\"", id)
          ), new Recipient(NodeType.ROOT)));
          return Optional.empty();
        }
      }

      return Optional.empty();
    }
  }

  private Set<Predicate> findNewPredicates(Set<Formula> newPredicateFormulaCandidates) {
    try (Context context = new Context()) {
      Solver solver = context.mkSolver();
      Set<Predicate> newPredicateCandidates = this.counterexampleAnalysisNode.addPredicates(
        this.verificationUuid,
        newPredicateFormulaCandidates
      );

      return newPredicateCandidates.stream()
        .filter(newPredicateCandidate -> this.predicates.stream()
          .noneMatch(predicate -> newPredicateCandidate.equals(predicate, context, solver)))
        .collect(Collectors.toSet());
    }
  }

  public Optional<CounterexampleAnalysisResult> analyze() {
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
    Set<Formula> newPredicateCandidates = new HashSet<>();

    int componentIndex;
    for (componentIndex = components.size() - 1; componentIndex >= 0; componentIndex--) {
      Component component = components.get(componentIndex);
      List<Formula> formulas = counterexampleAnalysisHistory.getCurrentFormulas();

      switch (component) {
        case IfStatement ifStatement -> formulas.add((componentIndex == components.size() - 1 ||
          !components.get(componentIndex + 1).equals(ifStatement.getThenBody().getBodyComponents().getFirst()))
            ? new NotFormula(ifStatement.getConditionExpression().toFormula(variables))
            : ifStatement.getConditionExpression().toFormula(variables));
        case WhileStatement whileStatement -> formulas.add((componentIndex == components.size() - 1 ||
          !components.get(componentIndex + 1).equals(whileStatement.getBody().getBodyComponents().getFirst()))
            ? new NotFormula(whileStatement.getConditionExpression().toFormula(variables))
            : whileStatement.getConditionExpression().toFormula(variables));
        case AssertStatement assertStatement -> formulas.add((componentIndex == components.size() - 1)
          ? new NotFormula(assertStatement.getExpression().toFormula(variables))
          : assertStatement.getExpression().toFormula(variables));
        case DeclarationVariableStatement declarationVariableStatement -> {
          if (declarationVariableStatement.getDeclarationExpression().isPresent()) {
            formulas = formulas.stream()
              .map(formula -> formula.replace(
                declarationVariableStatement.getId(),
                declarationVariableStatement.getDeclarationExpression().get().toFormula(variables)
              ))
              .toList();
          }
        }
        case AssignmentStatement assignmentStatement -> {
          formulas = formulas.stream()
            .map(formula -> formula.replace(
              assignmentStatement.getId(),
              assignmentStatement.getAssignExpression().toFormula(variables)
            ))
            .toList();
        }
        case Input _ -> {}
        default -> {
          this.counterexampleAnalysisNode.sendMessage(new Message<>(new CounterexampleAnalysisFailed(
            this.counterexampleAnalysisNode.getUuid(),
            String.format("unsupported component \"%s\"", component.getClass().getSimpleName())
          ), new Recipient(NodeType.ROOT)));
          return Optional.empty();
        }
      }

      Optional<Set<Formula>> newPredicateCandidatesOptional = this.findNewPredicateCandidates(
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

    Path counterexamplePath = new Path(components.stream()
      .map(component -> new State(new Location(component.getLine(), component.getLinePosition())))
      .toList());

    if (newPredicateCandidates.isEmpty()) {
      return Optional.of(new CounterexampleAnalysisResult(counterexamplePath, this.variableAssignments));
    }

    if (!counterexamplePath.getStates().isEmpty()) {
      Set<Predicate> newPredicates = this.findNewPredicates(newPredicateCandidates);

      if (newPredicates.isEmpty()) {
        return Optional.empty();
      }

      Path counterexamplePivotPath = new Path(counterexamplePath.getStates().subList(0, componentIndex + 1));
      counterexamplePivotPath.getStates().getLast().setChecked(false);
      counterexamplePivotPath.getStates().getLast().setPredicates(Stream.concat(
        this.predicates.stream(),
        newPredicates.stream()
      ).collect(Collectors.toSet()));

      return Optional.of(new CounterexampleAnalysisResult(counterexamplePath, counterexamplePivotPath));
    }

    this.counterexampleAnalysisNode.sendMessage(new Message<>(
      new CounterexampleAnalysisFailed(this.counterexampleAnalysisNode.getUuid(), "empty counterexample"),
      new Recipient(NodeType.ROOT)
    ));

    return Optional.empty();
  }
}
