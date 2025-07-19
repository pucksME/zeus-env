package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.formula.Formula;
import zeus.shared.formula.unary.NotFormula;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.counterexampleanalysis.CounterexampleAnalysisFailed;
import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.Path;
import zeus.shared.message.payload.modelchecking.State;
import zeus.zeuscompiler.symboltable.VariableInformation;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Component;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.AssertStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.IfStatement;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.statements.WhileStatement;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CounterexampleAnalyzer {
  Path path;
  ClientCodeModule clientCodeModule;
  CounterexampleAnalysisNode counterexampleAnalysisNode;

  public CounterexampleAnalyzer(
    Path path,
    ClientCodeModule clientCodeModule,
    CounterexampleAnalysisNode counterexampleAnalysisNode
  ) {
    this.path = path;
    this.clientCodeModule = clientCodeModule;
    this.counterexampleAnalysisNode = counterexampleAnalysisNode;
  }

  public void analyze() {
    Optional<Map<String, VariableInformation>> variablesOptional = this.clientCodeModule.getVariables();

    if (variablesOptional.isEmpty()) {
      this.counterexampleAnalysisNode.sendMessage(new Message<>(
        new CounterexampleAnalysisFailed(
          this.counterexampleAnalysisNode.getUuid(),
          "code module variable information not present"
        ),
        new Recipient(NodeType.ROOT)
      ));
      return;
    }

    Map<String, VariableInformation> variables = variablesOptional.get();
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
        return;
      }

      components.add(componentSearchResultOptional.get().getComponent());
    }

    CounterexampleAnalysisHistory counterexampleAnalysisHistory = new CounterexampleAnalysisHistory();
    for (int i = components.size() - 1; i >= 0; i--) {
      Component component = components.get(i);
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
          return;
        }
      }
    }
  }
}
