package zeus.zeuscompiler.bootsspecification.compiler.visitors;

import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.BootsSpecification;
import zeus.zeuscompiler.bootsspecification.compiler.syntaxtree.ClassGenerator;
import zeus.zeuscompiler.grammars.BootsSpecificationBaseVisitor;
import zeus.zeuscompiler.grammars.BootsSpecificationParser;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.ClientSymbolTable;
import zeus.zeuscompiler.symboltable.ServerSymbolTable;

import java.util.List;

public class BootsSpecificationVisitor extends BootsSpecificationBaseVisitor<Object> {

  @Override
  public Object visitSpecification(BootsSpecificationParser.SpecificationContext ctx) {
    return new BootsSpecification(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.class_().stream().map(classContext -> (ClassGenerator) visit(classContext)).toList()
    );
  }

  @Override
  public Object visitClass(BootsSpecificationParser.ClassContext ctx) {
    String generatorCode = ctx.GENERATOR().getText();

    ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ServerSymbolTable.class).addBootsSpecificationClass(ctx.CLASS().getText());

    return new ClassGenerator(
      ctx.getStart().getLine(),
      ctx.getStart().getCharPositionInLine(),
      ctx.CLASS().getText(),
      generatorCode.substring(1, generatorCode.length() - 2)
    );
  }
}
