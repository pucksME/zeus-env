package zeus.zeusverifier.node.modelchecking;

import zeus.shared.message.Message;
import zeus.shared.message.payload.modelchecking.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.ClientCodeModule;
import zeus.zeusverifier.config.modelcheckingnode.ModelCheckingNodeConfig;
import zeus.zeusverifier.node.Node;
import zeus.zeusverifier.routing.NodeAction;
import zeus.zeusverifier.routing.RouteResult;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;

public class ModelCheckingNode extends Node<ModelCheckingNodeConfig> {
  ClientCodeModule codeModule;

  public ModelCheckingNode(ModelCheckingNodeConfig config) {
    super(config);
  }

  private RouteResult setCodeModuleRoute(Message<ClientCodeModule> message, Socket socket) {
    System.out.println("Running setCodeModuleRoute route");
    this.codeModule = message.getPayload();
    return new RouteResult(new Message<>(new SetCodeModuleResponse()));
  }

  private RouteResult startModelCheckingRoute(
    Message<StartModelCheckingRequest> message,
    Socket requestSocket
  ) {
    System.out.println("Running startModelChecking route");

    CodeModuleIterator codeModuleIterator = new CodeModuleIterator(this.codeModule);
    if (!codeModuleIterator.calibrate(message.getPayload().path())) {
      return new RouteResult(new Message<>(new CalibrationFailed(message.getPayload().path())), NodeAction.TERMINATE);
    }

    return new RouteResult(new Message<>(new StartModelCheckingResponse()));
  }

  @Override
  public NodeAction handleGatewayRequest(Message<?> message, Socket requestSocket) throws IOException {
    return this.processMessage(
      message,
      requestSocket,
      Map.of(
        ClientCodeModule.class, this::setCodeModuleRoute,
        StartModelCheckingRequest.class, this::startModelCheckingRoute
      )
    );
  }

  @Override
  public void start() throws IOException {
    this.startGatewayListener(this.getConfig().getRootNode());
  }
}
