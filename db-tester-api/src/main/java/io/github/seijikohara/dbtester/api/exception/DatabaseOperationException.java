package io.github.seijikohara.dbtester.api.exception;

import java.io.Serial;

/**
 * Signals that a database operation failed.
 *
 * <p>This exception is thrown when the framework fails to execute SQL operations such as INSERT,
 * UPDATE, DELETE, or TRUNCATE during test preparation or verification.
 *
 * <p>Common causes:
 *
 * <ul>
 *   <li>SQL syntax errors
 *   <li>Constraint violations (foreign key, unique, check)
 *   <li>Connection failures during operation
 *   <li>Invalid column or table references
 *   <li>Data type mismatches
 * </ul>
 */
public final class DatabaseOperationException extends DatabaseTesterException {

  /** Serial version UID for serialization compatibility. */
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public DatabaseOperationException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the underlying exception
   */
  public DatabaseOperationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the underlying exception
   */
  public DatabaseOperationException(final Throwable cause) {
    super(cause);
  }
}
