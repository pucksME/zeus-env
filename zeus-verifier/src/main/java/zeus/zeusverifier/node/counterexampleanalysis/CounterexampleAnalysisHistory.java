package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.formula.Formula;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CounterexampleAnalysisHistory {
  private List<List<Formula>> formulaHistory;

  public CounterexampleAnalysisHistory() {
    this.formulaHistory = new ArrayList<>();
  }

  public void addFormulas(List<Formula> formulas) {
    this.formulaHistory.add(formulas);
  }

  public List<Formula> getCurrentFormulas() {
    try {
      return new ArrayList<>(this.formulaHistory.getLast());
    } catch (NoSuchElementException noSuchElementException) {
      return new ArrayList<>();
    }
  }

  public List<Formula> getFormulaHistory(int index) {
    List<Formula> formulaHistory = new ArrayList<>();
    for (List<Formula> formulas : this.formulaHistory) {
      if (index >= 0 && index < formulas.size()) {
        formulaHistory.add(formulas.get(index));
      }
    }
    return formulaHistory;
  }
}
