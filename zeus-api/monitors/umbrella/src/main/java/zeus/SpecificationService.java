package zeus;

import zeus.specification.Action;
import zeus.specification.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SpecificationService {
  private static SpecificationService specificationService;
  private static ConcurrentHashMap<SpecificationIdentifier, List<Specification>> specifications;
  private static List<Request> requests;

  private SpecificationService() {
    SpecificationService.specifications = new ConcurrentHashMap<>();
    SpecificationService.requests = new ArrayList<>();
  }

  public static void initialize() {
    if (SpecificationService.specificationService == null) {
      SpecificationService.specificationService = new SpecificationService();
    }
  }

  public static void register(SpecificationIdentifier specificationIdentifier, Specification specification) {
    if (!SpecificationService.specifications.containsKey(specificationIdentifier)) {
      SpecificationService.specifications.put(specificationIdentifier, new ArrayList<>());
    }

    SpecificationService.specifications.get(specificationIdentifier).add(specification);
  }

  public void addRequest(Request request) {
    SpecificationService.requests.add(request);
  }

  public static List<Request> getRequests() {
    return requests;
  }

  public static boolean verify(SpecificationIdentifier specificationIdentifier) {
    if (!SpecificationService.specifications.containsKey(specificationIdentifier)) {
      return true;
    }

    for (Specification specification : SpecificationService.specifications.get(specificationIdentifier)) {
      boolean result = specification.verify();

      if (result && specification.getAction() == Action.BLOCK) {
        return false;
      }

      if (!result && specification.getAction() == Action.ALLOW) {
        return false;
      }

      if (result && specification.getAction() == Action.LOG) {
        // TODO: log
      }
    }

    return true;
  }
}
