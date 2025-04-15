package zeus.zeuscompiler.rain.compiler.syntaxtree;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import zeus.shared.message.Message;
import zeus.shared.message.payload.VerificationResult;
import zeus.shared.message.utils.MessageJsonDeserializer;
import zeus.shared.message.utils.MessageUtils;
import zeus.zeuscompiler.CompilerError;
import zeus.zeuscompiler.providers.ServiceProvider;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementException;
import zeus.zeuscompiler.rain.compiler.syntaxtree.exceptions.semanticanalysis.AmbiguousElementType;
import zeus.zeuscompiler.rain.dtos.*;
import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.SymbolTableService;
import zeus.zeuscompiler.symboltable.*;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.codemodules.CodeModule;
import zeus.zeuscompiler.thunder.compiler.syntaxtree.exceptions.verification.UnknownCodeModuleException;
import zeus.zeuscompiler.thunder.compiler.utils.CompilerPhase;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.Context;
import zeus.zeuscompiler.umbrellaspecification.compiler.syntaxtree.UmbrellaSpecification;
import zeus.zeuscompiler.utils.CompilerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.stream.Collectors;

public class Project extends Node {
  String clientName;
  List<Element> elements;
  List<View> views;
  List<Server> servers;

  public Project(
    int line,
    int linePosition,
    String name,
    String clientName,
    List<Element> elements,
    List<View> views,
    List<Server> servers
  ) {
    super(line, linePosition, name);
    this.clientName = clientName;
    this.elements = elements;
    this.views = views;
    this.servers = servers;
  }

  private void checkServers() {
    Set<String> serverNames = new HashSet<>();

    for (Server server : this.servers) {
      if (serverNames.add(server.name)) {
        continue;
      }

      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        server.line,
        server.linePosition,
        new AmbiguousElementException(server.name, AmbiguousElementType.SERVER),
        CompilerPhase.TYPE_CHECKER
      ));
    }
  }

  @Override
  public void check() {
    ClientSymbolTable clientSymbolTable = ServiceProvider
      .provide(SymbolTableService.class).getContextSymbolTableProvider()
      .provide(ClientSymbolTable.class);

    for (Element element : this.elements) {
      if (clientSymbolTable.addBlueprintComponent((BlueprintComponent) element)) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new AmbiguousElementException(element.name, AmbiguousElementType.BLUEPRINT_COMPONENT),
          CompilerPhase.TYPE_CHECKER
        ));
      }

      clientSymbolTable.setCurrentComponent(element);
      element.check();
    }

    for (View view : this.views) {
      if (clientSymbolTable.addView(view)) {
        ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
          this.getLine(),
          this.getLinePosition(),
          new AmbiguousElementException(view.name, AmbiguousElementType.VIEW),
          CompilerPhase.TYPE_CHECKER
        ));
      }

      view.check();
    }

    this.checkServers();
  }

  public String translateViews(String appFileName, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(new String[]{"%s", "%s", "%s", "%s", "%s"}, 0),
        // https://reactrouter.com/en/main/start/tutorial#adding-a-router [accessed 09/01/2024, 17:31]
        "import { createBrowserRouter } from 'react-router-dom';",
        String.format(
          "import { %s } from './%s';",
          this.views.stream().map(view -> view.name).collect(Collectors.joining(", ")),
          appFileName
        ),
        "export const router = createBrowserRouter([",
        this.views.stream().map(view -> String.format(
          CompilerUtils.buildLinePadding(1) + "{ path: '/%s', element: <%s/> }",
          (view.isRoot) ? "" : view.name,
          view.name
        )).collect(Collectors.joining(",\n")),
        "]);"
      );
    };
  }

  public List<ExportedClientDto> translateClients(ExportTarget exportTarget) {
    if (this.clientName == null) {
      return new ArrayList<>();
    }

    String appFileName = switch (exportTarget) {
      case REACT_TYPESCRIPT -> "app.tsx";
    };

    return List.of(new ExportedClientDto(
      this.clientName,
      List.of(
        new ExportedFileDto(this.translate(0, exportTarget), appFileName),
        new ExportedFileDto(this.translateViews(appFileName, exportTarget), "views.tsx")
      )
    ));
  }

  public String translateUmbrellaSpecificationsInitialization() {
    ArrayList<String> code = new ArrayList<>();
    code.add("package zeus;");
    code.add("");
    code.add("import java.util.Set;");
    code.add("import zeus.specification.Context;");
    code.add("import zeus.specification.Action;");

    for (Server server : this.servers) {
      for (Route route : server.routes) {
        if (route.umbrellaSpecifications == null) {
          continue;
        }

        for (String id : route.umbrellaSpecifications.getUmbrellaSpecifications().keySet()) {
          code.add(String.format("import zeus.specification.Specification%s%s%s;", server.name, route.id, id));
        }
      }
    }

    code.add("");
    code.add("public class SpecificationInitializationService {");
    code.add(CompilerUtils.buildLinePadding(1) + "private SpecificationInitializationService() {");
    code.add(CompilerUtils.buildLinePadding(1) + "}");
    code.add("");
    code.add(CompilerUtils.buildLinePadding(1) + "public static void initialize(String context) {");

    for (Server server : this.servers) {
      for (Route route : server.routes) {
        if (route.umbrellaSpecifications == null) {
          continue;
        }

        for (Map.Entry<String, UmbrellaSpecification> entry : route.umbrellaSpecifications.getUmbrellaSpecifications().entrySet()) {
          code.add(CompilerUtils.buildLinePadding(2) + String.format(
            "SpecificationService.register(new SpecificationIdentifier(%s, \"%s\", \"%s\"), new Specification%s%s%s(\"%s\", \"%s\", \"%s\", %s, %s, %s));",
            (entry.getValue().getContext() == Context.GLOBAL) ? "\"global\"" : "context",
            server.name,
            route.id,
            server.name,
            route.id,
            entry.getKey(),
            server.name,
            route.id,
            entry.getKey(),
            entry.getValue().translateContext(),
            entry.getValue().translateActions(),
            entry.getValue().accessesResponse()
          ));
        }
      }
    }

    code.add(CompilerUtils.buildLinePadding(1) + "}");
    code.add("}");

    return String.join("\n", code);
  }

  public ExportedFileDto translateTypingMiddleware(ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> new ExportedFileDto(
        String.format(
          CompilerUtils.buildLinesFormat(
            new String[]{
              "export function typingMiddleware(req, res, next, routeId: string) {",
              "%s",
              "}"
            },
            0
          ),
          this.servers.stream()
            .map(server -> server.translateTypingMiddleware(exportTarget, 1))
            .collect(Collectors.joining("\n"))
        ),
        "typing.middleware.ts"
      );
    };
  }

  public List<ExportedServerDto> translateServers(ExportTarget exportTarget) {
    return this.servers.stream()
      .map(server -> new ExportedServerDto(
        server.name,
        List.of(
          new ExportedFileDto(server.translateConfiguration(0, exportTarget), "configuration.ts"),
          new ExportedFileDto(server.translate(0, exportTarget), "routes.ts")
        ),
        server.translateBootsSpecifications().entrySet().stream()
          .flatMap(bootsSpecificationTranslations -> bootsSpecificationTranslations.getValue().entrySet().stream()
            .map(bootsSpecificationTranslation -> new ExportedFileDto(
              bootsSpecificationTranslation.getValue(),
              String.format("%s-%s.py", bootsSpecificationTranslations.getKey(), bootsSpecificationTranslation.getKey())
            )))
          .toList(),
        server.translateUmbrellaSpecifications().entrySet().stream()
          .flatMap(serverUmbrellaSpecificationTranslations -> serverUmbrellaSpecificationTranslations.getValue().entrySet().stream()
            .map(routeUmbrellaSpecificationTranslations -> new ExportedFileDto(
              routeUmbrellaSpecificationTranslations.getValue(),
              String.format(
                "Specification%s%s%s.java",
                server.name,
                serverUmbrellaSpecificationTranslations.getKey(),
                routeUmbrellaSpecificationTranslations.getKey()
              )
            ))).toList()
      ))
      .toList();
  }

  @Override
  public String translate(int depth, ExportTarget exportTarget) {
    return switch (exportTarget) {
      case REACT_TYPESCRIPT -> String.format(
        CompilerUtils.buildLinesFormat(new String[]{"%s", "%s", "%s", "%s"}, depth),
        "import React, {CSSProperties} from 'react';",
        "type mutation = {style: CSSProperties, data?: {text?: string}}",
        CompilerUtils.trimLines(this.elements.stream().map(
          element -> element.translate(depth, exportTarget)
        ).collect(Collectors.joining("\n"))),
        this.views.stream().map(
          view -> view.translate(depth, exportTarget)
        ).collect(Collectors.joining("\n"))
      );
    };
  }

  public static Project fromDto(ExportProjectDto exportProjectDto) {
    return new Project(
      -1,
      -1,
      exportProjectDto.name(),
      exportProjectDto.name(),
      exportProjectDto.exportElementDtos().stream().map(
        exportElementDto -> Element.fromDto(exportElementDto, true)
      ).toList(),
      exportProjectDto.exportViewDtos().stream().map(View::fromDto).toList(),
      new ArrayList<>()
    );
  }

  public Optional<CodeModule> findCodeModule(String codeModuleName) {
    ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ClientSymbolTableIdentifier());

    Optional<CodeModule> codeModuleOptional = (this.clientName == null)
      ? Optional.empty()
      : ServiceProvider
        .provide(SymbolTableService.class).getContextSymbolTableProvider()
        .provide(ClientSymbolTable.class).getCodeModules().getCodeModule(codeModuleName);

    if (codeModuleOptional.isPresent()) {
      return codeModuleOptional;
    }

    for (Server server : this.servers) {
      for (Route route : server.routes) {
        ServiceProvider.provide(SymbolTableService.class).setContextSymbolTable(new ServerRouteSymbolTableIdentifier(
          server.name,
          route.getId())
        );

        codeModuleOptional = ServiceProvider
          .provide(SymbolTableService.class).getContextSymbolTableProvider()
          .provide(SymbolTable.class).getCodeModules().getCodeModule(codeModuleName);

        if (codeModuleOptional.isPresent()) {
          return codeModuleOptional;
        }
      }
    }

    return Optional.empty();
  }

  public void verify(String codeModuleName) {
    Optional<CodeModule> codeModuleOptional = this.findCodeModule(codeModuleName);

    if (codeModuleOptional.isEmpty()) {
      ServiceProvider.provide(CompilerErrorService.class).addError(new CompilerError(
        new UnknownCodeModuleException(),
        CompilerPhase.VERIFIER
      ));
      return;
    }

    Gson gson = new Gson();
    Message<CodeModule> message = new Message<CodeModule>(codeModuleOptional.get());
    String json = message.toJsonString();
    System.out.println(json);
    try {
      try (Socket socket = new Socket("localhost", 8081)) {
        PrintWriter outputPrintWriter = new PrintWriter(socket.getOutputStream(), true);
        outputPrintWriter.println(json);
        Message<VerificationResult> response = new GsonBuilder()
          .registerTypeAdapter(Message.class, new MessageJsonDeserializer<VerificationResult>())
          .create().fromJson(
            MessageUtils.readMessage(socket.getInputStream()),
            Message.class
          );
      }
    }  catch (UnknownHostException unknownHostException) {
      throw new RuntimeException("Could not send verifier request: unknown host");
    } catch (IOException ioException) {
      throw new RuntimeException("Could not send verifier request: io exception");
    }
  }
}
