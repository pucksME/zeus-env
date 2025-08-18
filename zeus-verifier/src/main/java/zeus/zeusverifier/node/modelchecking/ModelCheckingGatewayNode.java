package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.NodeSelection;
import zeus.shared.message.Recipient;
import zeus.shared.message.payload.NodeType;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.VerificationResult;
import zeus.shared.message.payload.modelchecking.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingGatewayNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ModelCheckingGatewayNode extends GatewayNode<ModelCheckingGatewayNodeConfig> {
  private final ConcurrentHashMap<UUID, AtomicInteger> runningModelCheckingTasksCounts;

  public ModelCheckingGatewayNode(ModelCheckingGatewayNodeConfig config) {
    super(config, NodeType.MODEL_CHECKING);
    this.runningModelCheckingTasksCounts = new ConcurrentHashMap<>();
  }

  private void initializeModelCheckingTasksCount(UUID verificationUuid) {
    this.runningModelCheckingTasksCounts.put(verificationUuid, new AtomicInteger(0));
  }

  private void startModelCheckingTask(UUID verificationUuid, UUID modelCheckingTaskUuid) {
    AtomicInteger runningModelCheckingTasksCount = this.runningModelCheckingTasksCounts.get(verificationUuid);
    if (runningModelCheckingTasksCount == null) {
      this.sendMessage(new Message<>(
        new ModelCheckingFailed(
          this.getUuid(),
          String.format(
            "model checking tasks count for verification uuid \"%s\" not present when trying to start a model checking task",
            verificationUuid
          )
        ),
        new Recipient(NodeType.ROOT)
      ));
      return;
    }

    int taskCount = runningModelCheckingTasksCount.incrementAndGet();
    System.out.printf(
      "Started new model checking task \"%s\": currently running model checking tasks: %d\n",
      modelCheckingTaskUuid,
      taskCount
    );
  }

  private void stopModelCheckingTask(
    UUID verificationUuid,
    UUID modelCheckingTaskUuid,
    StopModelCheckingTaskRequestStatus stopModelCheckingTaskRequestStatus
  ) {
    AtomicInteger runningModelCheckingTasksCount = this.runningModelCheckingTasksCounts.get(verificationUuid);
    if (runningModelCheckingTasksCount == null) {
      this.sendMessage(new Message<>(
        new ModelCheckingFailed(
          this.getUuid(),
          String.format(
            "model checking tasks count for verification uuid \"%s\" not present when trying to stop a model checking task",
            verificationUuid
          )
        ),
        new Recipient(NodeType.ROOT)
      ));
      return;
    }

    int taskCount = runningModelCheckingTasksCount.decrementAndGet();
    System.out.printf(
      "Stopped model checking task \"%s\" (\"%s\"): currently running model checking tasks: %d\n",
      modelCheckingTaskUuid,
      stopModelCheckingTaskRequestStatus,
      taskCount
    );

    if (taskCount == 0) {
      this.sendMessage(new Message<>(new VerificationResult(verificationUuid), new Recipient(NodeType.ROOT)));
    }
  }

  private RouteResult processClientCodeModuleRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running processClientCodeModuleRoute");

    return new RouteResult(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ALL)
    ));
  }

  private RouteResult processStartModelCheckingRequestRoute(Message<StartModelCheckingTaskRequest> message, Socket requestSocket) {
    this.initializeModelCheckingTasksCount(message.getPayload().getVerificationUuid());
    this.startModelCheckingTask(message.getPayload().getVerificationUuid(), message.getPayload().getUuid());

    return new RouteResult(new Message<>(
      message.getPayload(),
      new Recipient(NodeType.MODEL_CHECKING, NodeSelection.ANY)
    ));
  }

  private RouteResult processDistributeModelCheckingRequestRoute(
    Message<DistributeModelCheckingRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running processDistributeModelCheckingRequestRoute route");

    for (StartModelCheckingTaskRequest startModelCheckingTaskRequest : message.getPayload().getStartModelCheckingRequests()) {
      this.startModelCheckingTask(message.getPayload().getVerificationUuid(), startModelCheckingTaskRequest.getUuid());
      this.sendMessageToNode(new Message<>(startModelCheckingTaskRequest));
    }

    return new RouteResult();
  }

  private RouteResult processStopModelCheckingTaskRoute(Message<StopModelCheckingTaskRequest> message, Socket requestSocket) {
    this.stopModelCheckingTask(
      message.getPayload().verificationUuid(),
      message.getPayload().modelCheckingTaskUuid(),
      message.getPayload().status()
    );
    return new RouteResult();
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNode.class, this::registerNodeRoute,
        DistributeModelCheckingRequest.class, this::processDistributeModelCheckingRequestRoute,
        StopModelCheckingTaskRequest.class, this::processStopModelCheckingTaskRoute
      )
    );
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::processClientCodeModuleRoute,
        StartModelCheckingTaskRequest.class, this::processStartModelCheckingRequestRoute,
        DistributeModelCheckingRequest.class, this::processDistributeModelCheckingRequestRoute,
        StopModelCheckingTaskRequest.class, this::processStopModelCheckingTaskRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    new Thread(() -> {
      try {
        this.startGatewayServer();
      } catch (IOException ioException) {
        throw new RuntimeException(ioException);
      }
    }).start();

    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
