package io.github.seijikohara.dbtester.internal.jdbc.wrapper;

import io.github.seijikohara.dbtester.api.exception.DatabaseOperationException;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * JDBC operation utilities that convert SQLException to DatabaseOperationException.
 *
 * <p>This class provides utilities for wrapping JDBC operations that throw SQLException into
 * standard functional interfaces. All SQLException instances are converted to unchecked
 * DatabaseOperationException, enabling cleaner functional programming patterns.
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * import static io.github.seijikohara.dbtester.internal.jdbc.wrapper.Jdbc.*;
 *
 * // Resource management with try-with-resources
 * try (var connectionResource = open(dataSource::getConnection)) {
 *   run(() -> connectionResource.value().setAutoCommit(false));
 *   var metadata = get(connectionResource.value()::getMetaData);
 * }
 *
 * // Direct execution
 * run(() -> preparedStatement.setInt(1, value));
 * var result = get(preparedStatement::executeQuery);
 * }</pre>
 *
 * <p>This class is stateless and thread-safe.
 */
public final class Jdbc {

  /** Private constructor to prevent instantiation. */
  private Jdbc() {}

  /**
   * A resource wrapper that converts SQLException to DatabaseOperationException on close.
   *
   * <p>This record wraps an AutoCloseable resource and ensures that any SQLException thrown during
   * close is converted to an unchecked DatabaseOperationException. This enables clean
   * try-with-resources usage without SQLException declarations.
   *
   * @param <T> the type of the wrapped resource
   * @param value the wrapped resource
   */
  public record Resource<T extends AutoCloseable>(T value) implements AutoCloseable {

    /**
     * Closes the wrapped resource.
     *
     * @throws DatabaseOperationException if closing fails
     */
    @Override
    public void close() {
      try {
        value.close();
      } catch (final SQLException e) {
        throw new DatabaseOperationException(e);
      } catch (final Exception e) {
        throw new DatabaseOperationException(e);
      }
    }
  }

  /**
   * An operation that accepts no input, returns no result, and may throw SQLException.
   *
   * <p>This is a functional interface for JDBC operations that take no arguments and return no
   * value.
   */
  @FunctionalInterface
  public interface ThrowingRunnable {

    /**
     * Performs this operation.
     *
     * @throws SQLException if a database access error occurs
     */
    void run() throws SQLException;
  }

  /**
   * A supplier of results that may throw SQLException.
   *
   * <p>This is a functional interface for JDBC operations that return a value without requiring
   * input.
   *
   * @param <T> the type of results supplied
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws SQLException if a database access error occurs
     */
    T get() throws SQLException;
  }

  /**
   * A function that accepts one argument, produces a result, and may throw SQLException.
   *
   * <p>This is a functional interface for JDBC operations that transform input to output.
   *
   * @param <T> the type of the input
   * @param <R> the type of the result
   */
  @FunctionalInterface
  public interface ThrowingFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     * @throws SQLException if a database access error occurs
     */
    R apply(T t) throws SQLException;
  }

  /**
   * An operation that accepts a single input argument, returns no result, and may throw
   * SQLException.
   *
   * <p>This is a functional interface for JDBC operations that consume input without returning a
   * value.
   *
   * @param <T> the type of the input
   */
  @FunctionalInterface
  public interface ThrowingConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws SQLException if a database access error occurs
     */
    void accept(T t) throws SQLException;
  }

  /**
   * Opens an AutoCloseable resource, wrapping SQLException.
   *
   * <p>The returned Resource can be used in try-with-resources statements. Any SQLException during
   * open or close is converted to DatabaseOperationException.
   *
   * @param <T> the type of the resource
   * @param supplier the supplier that provides the resource
   * @return a Resource wrapping the opened resource
   * @throws DatabaseOperationException if obtaining the resource fails
   */
  public static <T extends AutoCloseable> Resource<T> open(final ThrowingSupplier<T> supplier) {
    try {
      return new Resource<>(supplier.get());
    } catch (final SQLException e) {
      throw new DatabaseOperationException(e);
    }
  }

  /**
   * Executes a SQL operation that returns no result.
   *
   * @param runnable the operation to execute
   * @throws DatabaseOperationException if a database error occurs
   */
  public static void run(final ThrowingRunnable runnable) {
    try {
      runnable.run();
    } catch (final SQLException e) {
      throw new DatabaseOperationException(e);
    }
  }

  /**
   * Executes a SQL operation that returns a result.
   *
   * @param <T> the type of result
   * @param supplier the operation to execute
   * @return the result of the operation
   * @throws DatabaseOperationException if a database error occurs
   */
  public static <T> T get(final ThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (final SQLException e) {
      throw new DatabaseOperationException(e);
    }
  }

  /**
   * Wraps a ThrowingConsumer into a standard Consumer.
   *
   * <p>This is useful for forEach operations where exceptions need to be handled uniformly.
   *
   * @param <T> the type of the input
   * @param consumer the throwing consumer to wrap
   * @return a Consumer that wraps SQLException in DatabaseOperationException
   */
  public static <T> Consumer<T> wrapConsumer(final ThrowingConsumer<T> consumer) {
    return t -> {
      try {
        consumer.accept(t);
      } catch (final SQLException e) {
        throw new DatabaseOperationException(e);
      }
    };
  }

  /**
   * Wraps a ThrowingFunction into a standard Function.
   *
   * <p>This is useful for map operations in streams where JDBC calls need to be made.
   *
   * @param <T> the type of the input
   * @param <R> the type of the result
   * @param function the throwing function to wrap
   * @return a Function that wraps SQLException in DatabaseOperationException
   */
  public static <T, R> Function<T, R> wrapFunction(final ThrowingFunction<T, R> function) {
    return t -> {
      try {
        return function.apply(t);
      } catch (final SQLException e) {
        throw new DatabaseOperationException(e);
      }
    };
  }

  /**
   * Wraps a ThrowingSupplier into a standard Supplier.
   *
   * @param <T> the type of results supplied
   * @param supplier the throwing supplier to wrap
   * @return a Supplier that wraps SQLException in DatabaseOperationException
   */
  public static <T> Supplier<T> wrapSupplier(final ThrowingSupplier<T> supplier) {
    return () -> {
      try {
        return supplier.get();
      } catch (final SQLException e) {
        throw new DatabaseOperationException(e);
      }
    };
  }
}
