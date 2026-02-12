package io.github.seijikohara.dbtester.api.assertion;

import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.spi.QueryAssertionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

/**
 * Test stub implementation of {@link QueryAssertionProvider} that records method invocations.
 *
 * <p>This provider records each method call with its arguments, enabling tests to verify that
 * {@link DatabaseQueryAssertion} correctly delegates to the underlying provider. The recorded
 * invocations can be inspected via {@link #getInvocations()} and cleared via {@link #reset()}.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} using the corresponding service
 * configuration file in {@code META-INF/services/}.
 */
public final class TestQueryAssertionProvider implements QueryAssertionProvider {

  /** Recorded method invocations. */
  private static final List<Invocation> INVOCATIONS =
      Collections.synchronizedList(new ArrayList<>());

  /** Creates a new TestQueryAssertionProvider instance. */
  public TestQueryAssertionProvider() {}

  /**
   * Returns an unmodifiable view of the recorded invocations.
   *
   * @return list of recorded invocations
   */
  static List<Invocation> getInvocations() {
    return List.copyOf(INVOCATIONS);
  }

  /**
   * Returns the most recent invocation.
   *
   * @return the last recorded invocation
   * @throws IllegalStateException if no invocations have been recorded
   */
  static Invocation getLastInvocation() {
    if (INVOCATIONS.isEmpty()) {
      throw new IllegalStateException("No invocations recorded");
    }
    return INVOCATIONS.get(INVOCATIONS.size() - 1);
  }

  /** Clears all recorded invocations. */
  static void reset() {
    INVOCATIONS.clear();
  }

  @Override
  public void assertEqualsByQuery(
      final TableSet expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    INVOCATIONS.add(
        new Invocation(
            "assertEqualsByQuery(TableSet,DataSource,String,String,Collection)",
            List.of(expected, dataSource, tableName, sqlQuery, ignoreColumnNames)));
  }

  @Override
  public void assertEqualsByQuery(
      final Table expected,
      final DataSource dataSource,
      final String tableName,
      final String sqlQuery,
      final Collection<String> ignoreColumnNames) {
    INVOCATIONS.add(
        new Invocation(
            "assertEqualsByQuery(Table,DataSource,String,String,Collection)",
            List.of(expected, dataSource, tableName, sqlQuery, ignoreColumnNames)));
  }

  /**
   * Represents a recorded method invocation.
   *
   * @param methodName the method signature
   * @param arguments the arguments passed to the method
   */
  record Invocation(String methodName, List<Object> arguments) {}
}
