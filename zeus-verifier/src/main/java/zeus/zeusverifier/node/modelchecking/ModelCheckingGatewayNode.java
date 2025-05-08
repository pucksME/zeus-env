package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.payload.RegisterNode;
import zeus.shared.message.payload.VerificationResponse;
import zeus.shared.message.payload.modelchecking.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingGatewayNodeConfig;
import zeus.zeusverifier.config.rootnode.RootNodeConfig;
import zeus.zeusverifier.node.GatewayNode;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.*;

public class ModelCheckingGatewayNode extends GatewayNode<ModelCheckingGatewayNodeConfig> {
  public ModelCheckingGatewayNode(ModelCheckingGatewayNodeConfig config) {
    super(config);
  }

  private RouteResult verifyRoute(Message<ClientCodeModule> message, Socket requestSocket) {
    System.out.println("Running verify route");

    if (this.getNodes().isEmpty()) {
      System.out.println("Could not verify code module: no model checking nodes available");
      return new RouteResult(new Message<>(new VerificationResponse(false)));
    }

    this.getNodes().forEach((uuid, socket) -> {
      try {
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
        printWriter.println(new Message<>(message.getPayload()).toJsonString());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    UUID nodeUuid = this.getNodes().keys().nextElement();
    try {
      PrintWriter printWriter = new PrintWriter(this.getNodes().get(nodeUuid).getOutputStream(), true);
      printWriter.println(new Message<>(new StartModelCheckingRequest(new Path(
        new ArrayList<>(),
        new HashSet<>()
      ))).toJsonString());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return new RouteResult(new Message<>(new VerificationResponse(false)));
  }

  private RouteResult processSetCodeModuleResponseRoute(Message<SetCodeModuleResponse> message, Socket requestSocket) {
    System.out.println("Running processSetCodeModuleResponse route");
    return new RouteResult();
  }

  private RouteResult processStartModelCheckingResponseRoute(
    Message<StartModelCheckingResponse> message,
    Socket requestSocket
  ) {
    System.out.println("Running processStartModelCheckingResponseRoute route");
    return new RouteResult();
  }

  private RouteResult processCalibrationFailedRoute(Message<CalibrationFailed> message, Socket requestSocket) {
    System.out.println("Model checking node \"%s\" could not calibrate path:");
    System.out.println(message.getPayload().path());
    return new RouteResult(NodeAction.TERMINATE);
  }

  @Override
  public NodeAction handleGatewayServerRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        RegisterNode.class, this::registerNodeRoute,
        ClientCodeModule.class, this::verifyRoute,
//        RegisterModelCheckingNodeRequest.class, this::registerModelCheckingNodeRoute,
        SetCodeModuleResponse.class, this::processSetCodeModuleResponseRoute,
        StartModelCheckingResponse.class, this::processStartModelCheckingResponseRoute,
        CalibrationFailed.class, this::processCalibrationFailedRoute
      )
    );
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of()
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
