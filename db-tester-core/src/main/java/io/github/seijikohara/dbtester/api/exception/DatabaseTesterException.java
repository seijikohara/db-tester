package io.github.seijikohara.dbtester.api.exception;

import java.io.Serial;

/**
 * Base class for all unchecked exceptions thrown by the framework.
 *
 * <p>All framework-specific exceptions extend this class to provide a common exception hierarchy.
 * This allows callers to catch all framework exceptions with a single catch block if desired, while
 * still maintaining specific exception types for fine-grained error handling.
 *
 * <h2>Framework Exception Hierarchy</h2>
 *
 * <ul>
 *   <li>{@link DataSetLoadException} - Data loading and parsing failures
 *   <li>{@link DataSourceNotFoundException} - Missing DataSource registration
 *   <li>{@link ValidationException} - Data validation and assertion failures
 * </ul>
 *
 * <p>This exception is unchecked (extends {@link RuntimeException}) to avoid forcing callers to
 * handle exceptions they may not be able to recover from.
 */
public class DatabaseTesterException extends RuntimeException {

  /** Serial version UID for serialization compatibility. */
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public DatabaseTesterException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the underlying exception
   */
  public DatabaseTesterException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the underlying exception
   */
  public DatabaseTesterException(final Throwable cause) {
    super(cause);
  }
}
