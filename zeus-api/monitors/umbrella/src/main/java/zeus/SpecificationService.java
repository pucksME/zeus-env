package zeus;

import zeus.specification.Action;
import zeus.specification.InvalidBooleanVariableValueException;
import zeus.specification.Specification;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class SpecificationService {
  private static SpecificationService specificationService;
  private static ConcurrentHashMap<SpecificationIdentifier, List<Specification>> specifications;

  private SpecificationService() {
    SpecificationService.specifications = new ConcurrentHashMap<>();
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

    if (SpecificationService.specifications.get(specificationIdentifier).contains(specification)) {
      return;
    }

    SpecificationService.specifications.get(specificationIdentifier).add(specification);
  }

  public static boolean verify(Request request, SpecificationIdentifier specificationIdentifier) {
    SpecificationIdentifier specificationIdentifierGlobal = new SpecificationIdentifier(
      "global",
      specificationIdentifier.serverName,
      specificationIdentifier.routeId
    );

    if (!SpecificationService.specifications.containsKey(specificationIdentifier) &&
      !SpecificationService.specifications.containsKey(specificationIdentifierGlobal)) {
      return true;
    }

    for (Specification specification : Stream.concat(
      SpecificationService.specifications.getOrDefault(specificationIdentifier, new ArrayList<>()).stream(),
      SpecificationService.specifications.getOrDefault(specificationIdentifierGlobal, new ArrayList<>()).stream()
    ).toList()) {
      boolean result;
      try {
        result = specification.verify(request);
      } catch (NoSuchElementException noSuchElementException) {
        return false;
      } catch (NumberFormatException numberFormatException) {
        return false;
      } catch (InvalidBooleanVariableValueException invalidBooleanVariableValueException) {
        return false;
      }

      if (!result && specification.getAction() == Action.BLOCK) {
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
