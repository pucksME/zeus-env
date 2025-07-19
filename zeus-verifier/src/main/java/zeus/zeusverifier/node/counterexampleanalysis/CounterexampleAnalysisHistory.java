package zeus.zeusverifier.node.counterexampleanalysis;

import zeus.shared.formula.Formula;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class CounterexampleAnalysisHistory {
  private ArrayList<ArrayList<Formula>> formulaHistory;

  public CounterexampleAnalysisHistory() {
    this.formulaHistory = new ArrayList<>();
  }

  public void addFormulas(ArrayList<Formula> formulas) {
    this.formulaHistory.add(formulas);
  }

  public List<Formula> getCurrentFormulas() {
    try {
      return new ArrayList<>(this.formulaHistory.getLast());
    } catch (NoSuchElementException noSuchElementException) {
      return new ArrayList<>();
    }
  }
}
