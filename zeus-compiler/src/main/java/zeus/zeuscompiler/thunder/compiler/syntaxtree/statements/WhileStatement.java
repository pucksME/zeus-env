package zeus.zeuscompiler.thunder.compiler.syntaxtree.statements;

import zeus.shared.message.payload.modelchecking.Location;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.dtos.ExportTarget;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.Body;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.BodyComponent;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.typechecking.IncompatibleTypeException;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.Expression;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.expressions.LiteralType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.PrimitiveType;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.types.Type;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.thunder.compiler.utils.ComponentSearchResult;
import zeus.zeuscompiler.thunder.compiler.utils.ParentStatement;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Collectors;

public class WhileStatement extends Statement {
  Expression conditionExpression;
  Body body;

  public WhileStatement(int line, int linePosition, Expression conditionExpression, Body body) {
    super(line, linePosition);
    this.conditionExpression = conditionExpression;
    this.body = body;
  }

  @Override
  public void checkTypes() {
    Optional<Type> conditionTypeOptional = this.conditionExpression.evaluateType();

    if (conditionTypeOptional.isEmpty()) {
      return;
    }

    Type conditionType = conditionTypeOptional.get();

    if (!(conditionType instanceof PrimitiveType) || ((PrimitiveType) conditionType).getType() != LiteralType.BOOLEAN) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        this.conditionExpression.getLine(),
        this.conditionExpression.getLinePosition(),
        new IncompatibleTypeException(),
        CompilerPhase.TYPE_CHECKER
      ));
    }

    for (BodyComponent bodyComponent : this.body.getBodyComponents()) {
      bodyComponent.checkTypes();
    }
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(
          new String[]{
            "while (%s) {",
            CompilerUtils.buildLinePadding(depth + 2) + "%s",
            CompilerUtils.buildLinePadding(depth + 1) + "}"
          },
          0
        ),
        this.conditionExpression.translate(depth, exportTarget),
        this.body.getBodyComponents().stream().map(
          bodyComponent -> bodyComponent.translate(depth + 1, exportTarget)
        ).collect(Collectors.joining("\n" + CompilerUtils.buildLinePadding(depth + 2)))
      );
    };
  }

  @Override
  public Optional<ComponentSearchResult> searchComponent(Location location, int index, Queue<ParentStatement> parents) {
    Optional<ComponentSearchResult> componentSearchResultOptional = super.searchComponent(location, index, parents);
    if (componentSearchResultOptional.isPresent()) {
      return componentSearchResultOptional;
    }

    List<BodyComponent> bodyComponents = this.body.getBodyComponents();
    for (int i = 0; i < bodyComponents.size(); i++) {
      parents = new LinkedList<>(parents);
      parents.add(new ParentStatement(this, index));
      componentSearchResultOptional = bodyComponents.get(i).searchComponent(location, i, parents);
      if (componentSearchResultOptional.isPresent()) {
        return componentSearchResultOptional;
      }
    }
    return Optional.empty();
  }
}
