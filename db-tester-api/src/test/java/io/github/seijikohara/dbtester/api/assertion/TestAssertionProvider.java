package io.github.seijikohara.dbtester.api.assertion;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.dataset.Table;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.spi.AssertionProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Test stub implementation of {@link AssertionProvider} that records method invocations.
 *
 * <p>This provider records each method call with its arguments, enabling tests to verify that
 * {@link DatabaseAssertion} correctly delegates to the underlying provider. The recorded
 * invocations can be inspected via {@link #getInvocations()} and cleared via {@link #reset()}.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} using the corresponding service
 * configuration file in {@code META-INF/services/}.
 */
public final class TestAssertionProvider implements AssertionProvider {

  /** Recorded method invocations. */
  private static final List<Invocation> INVOCATIONS =
      Collections.synchronizedList(new ArrayList<>());

  /** Creates a new TestAssertionProvider instance. */
  public TestAssertionProvider() {}

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
  public void assertEquals(final TableSet expected, final TableSet actual) {
    INVOCATIONS.add(new Invocation("assertEquals(TableSet,TableSet)", List.of(expected, actual)));
  }

  @Override
  public void assertEquals(
      final TableSet expected,
      final TableSet actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    INVOCATIONS.add(
        new Invocation(
            "assertEquals(TableSet,TableSet,AssertionFailureHandler)",
            List.of(expected, actual, wrapNullable(failureHandler))));
  }

  @Override
  public void assertEquals(final Table expected, final Table actual) {
    INVOCATIONS.add(new Invocation("assertEquals(Table,Table)", List.of(expected, actual)));
  }

  @Override
  public void assertEquals(
      final Table expected, final Table actual, final Collection<String> additionalColumnNames) {
    INVOCATIONS.add(
        new Invocation(
            "assertEquals(Table,Table,Collection)",
            List.of(expected, actual, additionalColumnNames)));
  }

  @Override
  public void assertEquals(
      final Table expected,
      final Table actual,
      final @Nullable AssertionFailureHandler failureHandler) {
    INVOCATIONS.add(
        new Invocation(
            "assertEquals(Table,Table,AssertionFailureHandler)",
            List.of(expected, actual, wrapNullable(failureHandler))));
  }

  @Override
  public void assertEqualsIgnoreColumns(
      final TableSet expected,
      final TableSet actual,
      final String tableName,
      final Collection<String> ignoreColumnNames) {
    INVOCATIONS.add(
        new Invocation(
            "assertEqualsIgnoreColumns(TableSet,TableSet,String,Collection)",
            List.of(expected, actual, tableName, ignoreColumnNames)));
  }

  @Override
  public void assertEqualsIgnoreColumns(
      final Table expected, final Table actual, final Collection<String> ignoreColumnNames) {
    INVOCATIONS.add(
        new Invocation(
            "assertEqualsIgnoreColumns(Table,Table,Collection)",
            List.of(expected, actual, ignoreColumnNames)));
  }

  @Override
  public void assertEqualsWithStrategies(
      final Table expected,
      final Table actual,
      final Collection<ColumnStrategyMapping> columnStrategies) {
    INVOCATIONS.add(
        new Invocation(
            "assertEqualsWithStrategies(Table,Table,Collection)",
            List.of(expected, actual, columnStrategies)));
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
   * Wraps a nullable value in a sentinel object for recording.
   *
   * @param value the nullable value
   * @return the value itself if non-null, or a sentinel string indicating null
   */
  private static Object wrapNullable(final @Nullable Object value) {
    return value != null ? value : "<<null>>";
  }

  /**
   * Represents a recorded method invocation.
   *
   * @param methodName the method signature
   * @param arguments the arguments passed to the method
   */
  record Invocation(String methodName, List<Object> arguments) {}
}
