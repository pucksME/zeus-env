package zeus;

import com.google.gson.JsonNull;
import zeus.specification.Action;
import zeus.specification.InvalidBooleanVariableValueException;
import zeus.specification.Specification;

import java.text.SimpleDateFormat;
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

    boolean block = false;
    for (Specification specification : Stream.concat(
      SpecificationService.specifications.getOrDefault(specificationIdentifier, new ArrayList<>()).stream(),
      SpecificationService.specifications.getOrDefault(specificationIdentifierGlobal, new ArrayList<>()).stream()
    ).toList()) {
      if (specification.accessesResponse() == (request.payload.responseBodyPayload instanceof JsonNull)) {
        continue;
      }

      boolean result = false;
      try {
        result = specification.verify(request);
      } catch (NoSuchElementException noSuchElementException) {
        block = true;
      } catch (NumberFormatException numberFormatException) {
        block = true;
      } catch (InvalidBooleanVariableValueException invalidBooleanVariableValueException) {
        block = true;
      }



      if (specification.getActions().contains(Action.LOG)) {
        System.out.printf(
          "[%s] Specification \"%s\" %s (%s)%n",
          new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()),
          specification.getId(),
          (result) ? "ok" : "violated",
          specification.actionsToString()
        );
      }

      if (result) {
        continue;
      }

      if (specification.getActions().contains(Action.BLOCK)) {
        block = true;
      }
    }

    return !block;
  }
}
