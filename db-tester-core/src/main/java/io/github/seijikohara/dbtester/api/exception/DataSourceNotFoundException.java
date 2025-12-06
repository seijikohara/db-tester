package io.github.seijikohara.dbtester.api.exception;

import java.io.Serial;

/**
 * Raised when a requested {@link javax.sql.DataSource} cannot be located in the registry.
 *
 * <p>This exception is thrown when attempting to retrieve a DataSource by name from the framework
 * registry, but no DataSource with the specified name has been registered.
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li>DataSource not registered before use
 *   <li>Incorrect DataSource name specified
 *   <li>Configuration not loaded or applied
 * </ul>
 */
public final class DataSourceNotFoundException extends DatabaseTesterException {

  /** Serial version UID for serialization compatibility. */
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public DataSourceNotFoundException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the underlying exception
   */
  public DataSourceNotFoundException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the underlying exception
   */
  public DataSourceNotFoundException(final Throwable cause) {
    super(cause);
  }
}
