package zeus.zeuscompiler.symboltable;

import java.util.ArrayList;
import java.util.List;

public class ServerSymbolTable extends SymbolTable {
  List<String> bootsSpecificationClasses;

  public ServerSymbolTable() {
    this.bootsSpecificationClasses = new ArrayList<>();
  }

  public void addBootsSpecificationClass(String id) {
    this.bootsSpecificationClasses.add(id);
  }

  public List<String> getBootsSpecificationClasses() {
    return bootsSpecificationClasses;
  }
}
