package zeus.zeusverifier.node.storage;

import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import zeus.shared.formula.Formula;
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

public class StorageNode extends Node<StorageNodeConfig> {
  private final ConcurrentHashMap<UUID, ConcurrentHashMap<Location, Set<Set<PredicateValuation>>>> visitedComponents;
  private final ConcurrentHashMap<UUID, Set<Predicate>> predicates;
  private final ConcurrentHashMap<UUID, ConcurrentHashMap<Set<PredicateValuation>, Boolean>> predicateValuationsAbstractValues;

  public StorageNode(StorageNodeConfig config) {
    super(config);
    this.visitedComponents = new ConcurrentHashMap<>();
    this.predicates = new ConcurrentHashMap<>();
    this.predicateValuationsAbstractValues = new ConcurrentHashMap<>();
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

  private Set<Formula> getFormulaCandidates(Set<Formula> formulas, Context context, Solver solver) {
    Set<Formula> formulaCandidates = new HashSet<>();

    for (Formula formula : formulas) {
      if (!formula.containsVariables()) {
        continue;
      }

      if (formulaCandidates.stream().noneMatch(formulaCandidate ->
        formulaCandidate.equals(formula, context, solver))) {
        formulaCandidates.add(formula);
      }
    }

    return formulaCandidates;
  }

  private Set<Predicate> getPredicates(
    Set<Formula> formulas,
    Set<Predicate> existingPredicates,
    Context context,
    Solver solver
  ) {
   Set<Predicate> predicates = new HashSet<>();

   for (Formula formula : formulas) {
      Predicate predicate = null;

      for (Predicate existingPredicate : existingPredicates) {
        if (formula.equals(existingPredicate.getFormula(), context, solver)) {
          predicate = existingPredicate;
          break;
        }
      }

      if (predicate == null) {
        predicate = Predicate.fromFormula(formula);
        existingPredicates.add(predicate);
      }

      predicates.add(predicate);
    }

    return predicates;
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

      return new RouteResult(new Message<>(
        new AddPredicatesResponse(
          message.getPayload().uuid(),
          this.getPredicates(
            this.getFormulaCandidates(message.getPayload().formulas(), context, solver),
            predicates,
            context,
            solver
          )
        ),
        new Recipient(NodeType.COUNTEREXAMPLE_ANALYSIS, message.getPayload().counterexampleAnalysisNodeUuid())
      ));
    }
  }

  private RouteResult processCheckPredicateValuationsRequestRoute(
    Message<CheckPredicateValuationsRequest> message,
    Socket socket
  ) {
    System.out.println("Running CheckPredicateValuationsRequestRoute route");

    Map<Set<PredicateValuation>, Boolean> predicateValuationsAbstractValue = this.predicateValuationsAbstractValues.get(
      message.getPayload().verificationUuid()
    );

    if (predicateValuationsAbstractValue == null) {
      return new RouteResult(new Message<>(
        new CheckPredicateValuationsResponse(message.getPayload().uuid()),
        new Recipient(NodeType.STORAGE_GATEWAY)
      ));
    }

    Boolean abstractValue = predicateValuationsAbstractValue.get(message.getPayload().predicateValuations());

    if (abstractValue == null) {
      return new RouteResult(new Message<>(
        new CheckPredicateValuationsResponse(message.getPayload().uuid()),
        new Recipient(NodeType.STORAGE_GATEWAY)
      ));
    }

    return new RouteResult(new Message<>(new CheckPredicateValuationsResponse(
      message.getPayload().uuid(),
      abstractValue
    ), new Recipient(NodeType.STORAGE_GATEWAY)));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        AddVisitedComponentRequest.class, this::processAddVisitedComponentRoute,
        CheckIfComponentVisitedRequest.class, this::processCheckIfComponentVisitedRoute,
        AddPredicatesRequest.class, this::processAddPredicatesRequestRoute,
        CheckPredicateValuationsRequest.class, this::processCheckPredicateValuationsRequestRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
