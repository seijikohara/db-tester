package io.github.seijikohara.dbtester.api.exception;

import java.io.Serial;

/**
 * Indicates that a database validation step observed a mismatch.
 *
 * <p>This exception is thrown when database content does not match the expected dataset during
 * validation operations. It typically indicates test failures where actual database state differs
 * from expected state.
 *
 * <p>Common causes:
 *
 * <ul>
 *   <li>Row count mismatch between expected and actual tables
 *   <li>Column value differences in table data
 *   <li>Missing or unexpected rows in database tables
 *   <li>Schema differences between expected and actual database
 * </ul>
 */
public final class ValidationException extends DatabaseTesterException {

  /** Serial version UID for serialization compatibility. */
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public ValidationException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the underlying exception
   */
  public ValidationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the underlying exception
   */
  public ValidationException(final Throwable cause) {
    super(cause);
  }
}
