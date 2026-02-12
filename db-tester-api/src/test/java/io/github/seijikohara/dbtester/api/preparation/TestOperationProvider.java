package io.github.seijikohara.dbtester.api.preparation;

import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.dataset.TableSet;
import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import io.github.seijikohara.dbtester.api.spi.OperationProvider;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;
import org.jspecify.annotations.Nullable;

/**
 * Test stub implementation of {@link OperationProvider} that records method invocations.
 *
 * <p>This provider records each method call with its arguments, enabling tests to verify that
 * {@link DatabasePreparation} correctly delegates to the underlying provider. The recorded
 * invocations can be inspected via {@link #getInvocations()} and cleared via {@link #reset()}.
 *
 * <p>This class is loaded via {@link java.util.ServiceLoader} using the corresponding service
 * configuration file in {@code META-INF/services/}.
 */
public final class TestOperationProvider implements OperationProvider {

  /** Recorded method invocations. */
  private static final List<Invocation> INVOCATIONS =
      Collections.synchronizedList(new ArrayList<>());

  /** Creates a new TestOperationProvider instance. */
  public TestOperationProvider() {}

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
  public void execute(
      final Operation operation,
      final TableSet tableSet,
      final DataSource dataSource,
      final TableOrderingStrategy tableOrderingStrategy,
      final TransactionMode transactionMode,
      final @Nullable Duration queryTimeout) {
    INVOCATIONS.add(
        new Invocation(
            "execute(6-arg)",
            List.of(
                operation,
                tableSet,
                dataSource,
                tableOrderingStrategy,
                transactionMode,
                wrapNullable(queryTimeout))));
  }

  @Override
  public void execute(
      final Operation operation,
      final TableSet tableSet,
      final DataSource dataSource,
      final TableOrderingStrategy tableOrderingStrategy,
      final TransactionMode transactionMode,
      final @Nullable Duration queryTimeout,
      final int batchSize) {
    INVOCATIONS.add(
        new Invocation(
            "execute(7-arg)",
            List.of(
                operation,
                tableSet,
                dataSource,
                tableOrderingStrategy,
                transactionMode,
                wrapNullable(queryTimeout),
                batchSize)));
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
