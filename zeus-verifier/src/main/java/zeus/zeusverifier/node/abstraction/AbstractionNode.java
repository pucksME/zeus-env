package zeus.zeusverifier.node.abstraction;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.abstraction.AbstractRequest;
import zeus.shared.message.payload.abstraction.AbstractResponse;
import zeus.shared.message.payload.abstraction.AbstractLiteral;
import zeus.shared.message.payload.abstraction.AbstractionFailed;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.predicate.Predicate;
import zeus.zeusverifier.config.abstractionnode.AbstractionNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class AbstractionNode extends Node<AbstractionNodeConfig> {
  public AbstractionNode(AbstractionNodeConfig config) {
    super(config);
  }

  private boolean prove(List<Expr> formulas, Solver solver, Context context) {
    return solver.check(context.mkNot(context.mkAnd(formulas.toArray(Expr[]::new)))) == Status.UNSATISFIABLE;
  }

  private boolean prove(List<Expr> formular, Expr expression, Solver solver, Context context) {
    return this.prove(Stream.concat(formular.stream(), Stream.of(expression)).toList(), solver, context);
  }

  private RouteResult processAbstractRequestRoute(Message<AbstractRequest> message, Socket requestSocket) {
    System.out.printf("Running processAbstractRequestRoute for uuid \"%s\"%n", message.getPayload().uuid());

    try (Context context = new Context()) {
      Solver solver = context.mkSolver();
      List<Expr> formulas = new ArrayList<>();

      for (Map.Entry<UUID, Predicate> uuidPredicate : message.getPayload().predicates().entrySet()) {
        PredicateValuation predicateValuation = message.getPayload().predicateValuations().get(uuidPredicate.getKey());

        if (predicateValuation == null) {
          System.out.printf(
            "Could not compute abstraction: missing valuation for predicate \"%s\"%n",
            uuidPredicate.getKey()
          );

          return new RouteResult(new Message<>(
            new AbstractionFailed(
              this.getUuid(),
              String.format("missing valuation for predicate \"%s\"", uuidPredicate.getKey())
            ),
            new Recipient(NodeType.ROOT)
          ));
        }

        formulas.add((predicateValuation.getValue())
          ? uuidPredicate.getValue().getFormula().toFormula(context)
          : context.mkNot(uuidPredicate.getValue().getFormula().toFormula(context)));
      }

      Expr expression = message.getPayload().expression().toFormula(context);

      if (this.prove(formulas, expression, solver, context)) {
        return new RouteResult(new Message<>(
          new AbstractResponse(message.getPayload().uuid(), AbstractLiteral.TRUE),
          new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
        ));
      }

      if (this.prove(formulas, context.mkNot(expression), solver, context)) {
        return new RouteResult(new Message<>(
          new AbstractResponse(message.getPayload().uuid(), AbstractLiteral.FALSE),
          new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
        ));
      }

    }

    return new RouteResult(new Message<>(
      new AbstractResponse(message.getPayload().uuid(), AbstractLiteral.NON_DETERMINISTIC),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
    ));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AbstractRequest.class, this::processAbstractRequestRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
