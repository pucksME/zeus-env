package zeus.zeuscompiler;

import java.util.Optional;

public class ServiceProvider {
  private static ServiceProvider serviceProvider;
  private static SymbolTableService symbolTableService;
  private static CompilerErrorService compilerErrorService;

  private ServiceProvider() {
  }

  public static void initialize() {
    if (ServiceProvider.serviceProvider == null) {
      ServiceProvider.serviceProvider = new ServiceProvider();
    }
  }

  public static void register(SymbolTableService symbolTableService) {
    ServiceProvider.symbolTableService = symbolTableService;
  }

  public static void register(CompilerErrorService compilerErrorService) {
    ServiceProvider.compilerErrorService = compilerErrorService;
  }

  public static <T extends Service> Optional<T> provide(Class<T> serviceClass) {
    if (serviceClass == SymbolTableService.class) {
      return Optional.of((T) ServiceProvider.symbolTableService);
    }

    if (serviceClass == CompilerErrorService.class) {
      return Optional.of((T) ServiceProvider.compilerErrorService);
    }

    throw new ServiceUnavailableException();
  }
}
