package zeus.zeusverifier.node.storage;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;
import zeus.shared.message.Message;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.modelchecking.Location;
import zeus.shared.message.payload.modelchecking.PredicateValuation;
import zeus.shared.message.payload.storage.*;
import zeus.shared.predicate.Predicate;
import zeus.zeusverifier.config.storagenode.StorageNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StorageNode extends Node<StorageNodeConfig> {
  private final ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Set<Set<PredicateValuation>>>> visitedComponents;
  private final ConcurrentHashMap<UUID, Set<Predicate>> predicates;

  public StorageNode(StorageNodeConfig config) {
    super(config);
    this.visitedComponents = new ConcurrentHashMap<>();
    this.predicates = new ConcurrentHashMap<>();
  }

  private RouteResult processAddVisitedComponentRoute(Message<AddVisitedComponentRequest> message, Socket socket) {
    System.out.println("Running AddVisitedComponentRoute route");
    ConcurrentHashMap<Location, Set<Set<PredicateValuation>>> visitedComponents = this.visitedComponents.get(
      message.getPayload().verificationUuid()
    );

    if (visitedComponents == null) {
      Set<Set<PredicateValuation>> predicateValuations = ConcurrentHashMap.newKeySet();
      predicateValuations.add(message.getPayload().predicateValuations());
      this.visitedComponents.put(message.getPayload().verificationUuid(), new ConcurrentHashMap<>(Map.of(message.getPayload().location(), predicateValuations)));
      return new RouteResult(new Message<>(
        new AddVisitedComponentResponse(message.getPayload().uuid(), false),
        new Recipient(NodeType.MODEL_CHECKING, message.getPayload().modelCheckingNodeUuid())
      ));
    }

    Set<Set<PredicateValuation>> predicateValuations = visitedComponents.get(message.getPayload().location());
    if (predicateValuations == null) {
      predicateValuations = ConcurrentHashMap.newKeySet();
      predicateValuations.add(message.getPayload().predicateValuations());
      visitedComponents.put(message.getPayload().location(), predicateValuations);
      return new RouteResult(new Message<>(
        new AddVisitedComponentResponse(message.getPayload().uuid(), false),
        new Recipient(NodeType.MODEL_CHECKING, message.getPayload().modelCheckingNodeUuid())
      ));
    }

    return new RouteResult(new Message<>(
      new AddVisitedComponentResponse(
        message.getPayload().uuid(),
        !predicateValuations.add(message.getPayload().predicateValuations())
      ),
      new Recipient(NodeType.MODEL_CHECKING, message.getPayload().modelCheckingNodeUuid())
    ));
  }

  private RouteResult processCheckIfComponentVisitedRoute(
    Message<CheckIfComponentVisitedRequest> message,
    Socket socket
  ) {
    System.out.println("Running CheckIfComponentVisitedRoute route");
    ConcurrentHashMap<Location, Set<Set<PredicateValuation>>> visitedComponents = this.visitedComponents.get(
      message.getPayload().verificationUuid()
    );

    if (visitedComponents == null) {
      return new RouteResult(new Message<>(new CheckIfComponentVisitedResponse(
        message.getPayload().uuid(),
        message.getPayload().location(),
        false
      ), new Recipient(NodeType.STORAGE_GATEWAY)));
    }

    Set<Set<PredicateValuation>> predicateValuations = visitedComponents.get(message.getPayload().location());
    return new RouteResult(new Message<>(new CheckIfComponentVisitedResponse(
      message.getPayload().uuid(),
      message.getPayload().location(),
      predicateValuations != null && predicateValuations.contains(message.getPayload().predicateValuations())
    ), new Recipient(NodeType.STORAGE_GATEWAY)));
  }

  private boolean predicatesEqual(Predicate predicate1, Predicate predicate2, Context context, Solver solver) {
    return solver.check(context.mkNot(context.mkEq(
      predicate1.getFormula().toFormula(context),
      predicate2.getFormula().toFormula(context)
    ))) == Status.UNSATISFIABLE;
  }

  private Set<Predicate> addPredicates(Set<Predicate> newPredicates, Set<Predicate> predicates, Context context, Solver solver) {
    Set<Predicate> addedPredicates = new HashSet<>();

    for (Predicate newPredicate : newPredicates) {
      if (!newPredicate.getFormula().containsVariables()) {
        continue;
      }

      if (predicates.stream().noneMatch(predicate ->
        this.predicatesEqual(predicate, newPredicate, context, solver))) {
        predicates.add(newPredicate);
        addedPredicates.add(newPredicate);
      }
    }

    return addedPredicates;
  }

  private RouteResult processAddPredicatesRequestRoute(Message<AddPredicatesRequest> message, Socket socket) {
    System.out.println("Running AddPredicatesRequestRoute route");
    Set<Predicate> predicates = this.predicates.get(message.getPayload().verificationUuid());
    if (predicates == null) {
      predicates = ConcurrentHashMap.newKeySet();
      this.predicates.put(message.getPayload().verificationUuid(), predicates);
    }

    try (Context context = new Context()) {
      Solver solver = context.mkSolver();
      Set<Predicate> predicatesDeduplicates = new HashSet<>();

      this.addPredicates(
        message.getPayload().formulas().stream().map(Predicate::fromFormula).collect(Collectors.toSet()),
        predicatesDeduplicates,
        context,
        solver
      );

      return new RouteResult(new Message<>(
        new AddPredicatesResponse(
          message.getPayload().uuid(),
          this.addPredicates(predicatesDeduplicates, predicates, context, solver)
        ),
        new Recipient(NodeType.COUNTEREXAMPLE_ANALYSIS, message.getPayload().counterexampleAnalysisNodeUuid())
      ));
    }
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AddVisitedComponentRequest.class, this::processAddVisitedComponentRoute,
        CheckIfComponentVisitedRequest.class, this::processCheckIfComponentVisitedRoute,
        AddPredicatesRequest.class, this::processAddPredicatesRequestRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
