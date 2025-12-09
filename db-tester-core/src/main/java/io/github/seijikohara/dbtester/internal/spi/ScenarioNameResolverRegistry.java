package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.api.scenario.ScenarioName;
import io.github.seijikohara.dbtester.api.scenario.ScenarioNameResolver;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Registry for discovering and selecting {@link ScenarioNameResolver} implementations.
 *
 * <p>This registry loads all available resolver implementations via {@link ServiceLoader} and
 * provides methods to resolve scenario names using the most appropriate resolver for a given test
 * method.
 *
 * <p>When resolving a scenario name, the registry:
 *
 * <ol>
 *   <li>Filters resolvers by {@link ScenarioNameResolver#canResolve(Method)}
 *   <li>Sorts matching resolvers by {@link ScenarioNameResolver#priority()} (descending)
 *   <li>Uses the highest-priority resolver that can handle the method
 *   <li>Falls back to {@link Method#getName()} if no resolver matches
 * </ol>
 *
 * <p>This class is thread-safe. The resolver list is loaded once during class initialization and is
 * immutable thereafter.
 *
 * @see ScenarioNameResolver
 */
public final class ScenarioNameResolverRegistry {

  /** Loaded resolvers sorted by priority (highest first). */
  private static final List<ScenarioNameResolver> RESOLVERS = loadResolvers();

  /** Private constructor to prevent instantiation. */
  private ScenarioNameResolverRegistry() {}

  /**
   * Resolves the scenario name for a test method.
   *
   * <p>Selects the highest-priority resolver that can handle the method and delegates to it. If no
   * resolver can handle the method, falls back to using the method name directly.
   *
   * @param testMethod the test method to resolve the scenario name from
   * @return the resolved scenario name
   */
  public static ScenarioName resolve(final Method testMethod) {
    return RESOLVERS.stream()
        .filter(resolver -> resolver.canResolve(testMethod))
        .findFirst()
        .map(resolver -> resolver.resolve(testMethod))
        .orElseGet(() -> new ScenarioName(testMethod.getName()));
  }

  /**
   * Returns all registered resolvers.
   *
   * <p>The returned list is sorted by priority (highest first) and is immutable.
   *
   * @return immutable list of registered resolvers
   */
  public static List<ScenarioNameResolver> getResolvers() {
    return RESOLVERS;
  }

  /**
   * Loads all resolver implementations via ServiceLoader.
   *
   * @return immutable list of resolvers sorted by priority (descending)
   */
  private static List<ScenarioNameResolver> loadResolvers() {
    return ServiceLoader.load(ScenarioNameResolver.class).stream()
        .map(ServiceLoader.Provider::get)
        .sorted(Comparator.comparingInt(ScenarioNameResolver::priority).reversed())
        .toList();
  }
}
