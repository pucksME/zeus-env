package zeus.zeuscompiler.providers;

import zeus.zeuscompiler.services.CompilerErrorService;
import zeus.zeuscompiler.services.Service;
import zeus.zeuscompiler.services.SymbolTableService;

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

  public static <T extends Service> T provide(Class<T> serviceClass) {
    if (serviceClass == SymbolTableService.class) {
      return (T) ServiceProvider.symbolTableService;
    }

    if (serviceClass == CompilerErrorService.class) {
      return (T) ServiceProvider.compilerErrorService;
    }

    throw new ServiceUnavailableException();
  }

  public static void resetServices() {
    ServiceProvider.symbolTableService.reset();
    ServiceProvider.compilerErrorService.reset();
  }
}
