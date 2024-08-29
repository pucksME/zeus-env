package zeus.zeuscompiler.symboltable;

import java.util.ArrayList;
import java.util.List;

public class ServerRouteSymbolTable extends SymbolTable {
  List<String> bootsSpecificationClasses;

  public ServerRouteSymbolTable() {
    this.bootsSpecificationClasses = new ArrayList<>();
  }

  public void addBootsSpecificationClass(String id) {
    this.bootsSpecificationClasses.add(id);
  }

  public List<String> getBootsSpecificationClasses() {
    return bootsSpecificationClasses;
  }
}
