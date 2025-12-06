package io.github.seijikohara.dbtester.junit.jupiter.spi;

import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import io.github.seijikohara.dbtester.internal.spi.ScenarioNameResolver;
import java.lang.reflect.Method;

/**
 * JUnit Jupiter implementation of {@link ScenarioNameResolver}.
 *
 * <p>This resolver provides scenario name resolution for JUnit Jupiter tests. In JUnit, the
 * scenario name is simply the test method name, as JUnit uses standard Java method naming
 * conventions.
 *
 * <p>For JUnit tests, this resolver returns {@link Method#getName()} directly. This matches the
 * convention where test method names like {@code shouldLoadData} correspond to scenario names in
 * CSV files.
 *
 * <p>This resolver uses the default priority ({@value ScenarioNameResolver#DEFAULT_PRIORITY}).
 * Framework-specific resolvers for Spock or Kotest should use higher priorities to take precedence
 * when applicable.
 *
 * <p>This resolver returns {@code true} for {@link #canResolve(Method)} for all methods, serving as
 * a fallback resolver. More specific resolvers (e.g., Spock) should check for framework-specific
 * annotations and return {@code false} for methods they cannot handle.
 *
 * @see ScenarioNameResolver
 */
public final class JUnitScenarioNameResolver implements ScenarioNameResolver {

  /** Creates a new JUnit scenario name resolver. */
  public JUnitScenarioNameResolver() {}

  /**
   * Resolves the scenario name from a JUnit test method.
   *
   * <p>Returns the method name directly, as JUnit uses standard Java method naming conventions.
   *
   * @param testMethod the test method to resolve the scenario name from
   * @return the scenario name based on the method name
   */
  @Override
  public ScenarioName resolve(final Method testMethod) {
    return new ScenarioName(testMethod.getName());
  }

  /**
   * Determines if this resolver can handle the given test method.
   *
   * <p>This implementation returns {@code true} for all methods, serving as a fallback resolver.
   * Framework-specific resolvers with higher priority should handle their specific cases first.
   *
   * @param testMethod the test method to check
   * @return always {@code true}
   */
  @Override
  public boolean canResolve(final Method testMethod) {
    return true;
  }

  /**
   * Returns the priority of this resolver.
   *
   * <p>Uses the default priority, allowing framework-specific resolvers to take precedence.
   *
   * @return {@link ScenarioNameResolver#DEFAULT_PRIORITY}
   */
  @Override
  public int priority() {
    return DEFAULT_PRIORITY;
  }
}
