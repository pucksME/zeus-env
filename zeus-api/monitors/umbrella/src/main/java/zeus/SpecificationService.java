package zeus;

import zeus.specification.Action;
import zeus.specification.InvalidBooleanVariableValueException;
import zeus.specification.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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

  public static void addRequest(SpecificationIdentifier specificationIdentifier, Request request) {
    SpecificationService.requests.add(request);
  }

  public static List<Request> getRequests(SpecificationIdentifier specificationIdentifier) {
    return requests;
  }

  public static boolean verify(Request request, SpecificationIdentifier specificationIdentifier) {
    if (!SpecificationService.specifications.containsKey(specificationIdentifier)) {
      return true;
    }

    for (Specification specification : SpecificationService.specifications.get(specificationIdentifier)) {
      boolean result = false;
      try {
        result = specification.verify(request, specificationIdentifier);
      } catch (NoSuchElementException noSuchElementException) {
        return false;
      } catch (NumberFormatException numberFormatException) {
        return false;
      } catch (InvalidBooleanVariableValueException invalidBooleanVariableValueException) {
        return false;
      }

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
