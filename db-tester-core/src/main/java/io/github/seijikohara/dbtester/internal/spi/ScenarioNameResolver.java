package io.github.seijikohara.dbtester.internal.spi;

import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.lang.reflect.Method;

/**
 * Service Provider Interface for resolving scenario names from test methods.
 *
 * <p>Different test frameworks have different conventions for test method naming. For example:
 *
 * <ul>
 *   <li><strong>JUnit:</strong> Uses standard Java method names (e.g., {@code shouldLoadData})
 *   <li><strong>Spock:</strong> Uses descriptive strings with spaces (e.g., {@code "should load
 *       data"}) stored in {@code @FeatureMetadata}
 *   <li><strong>Kotest:</strong> Uses lambda-based test names with descriptive strings
 * </ul>
 *
 * <p>This SPI allows each test framework module to provide its own implementation that correctly
 * extracts the scenario name according to its conventions.
 *
 * <h2>Implementation Discovery</h2>
 *
 * <p>Implementations are discovered via {@link java.util.ServiceLoader}. Each test framework module
 * (db-tester-junit, db-tester-spock, etc.) should:
 *
 * <ol>
 *   <li>Implement this interface
 *   <li>Register the implementation in {@code META-INF/services/}
 * </ol>
 *
 * <h2>Default Behavior</h2>
 *
 * <p>If no custom implementation is found, the framework falls back to using {@link
 * Method#getName()}, which works for standard JUnit tests but may not work correctly for other
 * frameworks.
 *
 * <h2>Example Implementation (Spock)</h2>
 *
 * <pre>{@code
 * public class SpockScenarioNameResolver implements ScenarioNameResolver {
 *
 *   @Override
 *   public ScenarioName resolve(Method testMethod) {
 *     var metadata = testMethod.getAnnotation(FeatureMetadata.class);
 *     var name = metadata != null ? metadata.name() : testMethod.getName();
 *     return new ScenarioName(name);
 *   }
 *
 *   @Override
 *   public int priority() {
 *     return 100; // Higher than default JUnit resolver
 *   }
 * }
 * }</pre>
 *
 * @see ScenarioName
 * @see java.util.ServiceLoader
 */
public interface ScenarioNameResolver {

  /**
   * Default priority for resolvers.
   *
   * <p>Resolvers with higher priority values are preferred when multiple implementations are
   * available. The default JUnit resolver typically uses priority 0.
   */
  int DEFAULT_PRIORITY = 0;

  /**
   * Resolves the scenario name from a test method.
   *
   * <p>Implementations should extract the appropriate name based on their framework's conventions.
   * For example, Spock implementations would read from {@code @FeatureMetadata}, while JUnit
   * implementations would simply return the method name.
   *
   * @param testMethod the test method to resolve the scenario name from
   * @return the resolved scenario name, never null
   */
  ScenarioName resolve(Method testMethod);

  /**
   * Determines if this resolver can handle the given test method.
   *
   * <p>Implementations should check for framework-specific markers (e.g., annotations) to determine
   * if they are the appropriate resolver for the method.
   *
   * <p>The default implementation returns {@code true}, meaning the resolver can handle any method.
   * Framework-specific implementations should override this to check for their specific markers.
   *
   * @param testMethod the test method to check
   * @return {@code true} if this resolver can handle the method, {@code false} otherwise
   */
  default boolean canResolve(Method testMethod) {
    return true;
  }

  /**
   * Returns the priority of this resolver.
   *
   * <p>When multiple resolvers can handle a test method (i.e., {@link #canResolve(Method)} returns
   * {@code true}), the one with the highest priority is used.
   *
   * <p>Framework-specific resolvers should return a higher priority than the default resolver to
   * ensure they are selected when applicable.
   *
   * @return the priority value; higher values indicate higher priority
   */
  default int priority() {
    return DEFAULT_PRIORITY;
  }
}
