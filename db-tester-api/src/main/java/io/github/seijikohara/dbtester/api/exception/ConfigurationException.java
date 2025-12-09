package io.github.seijikohara.dbtester.api.exception;

import java.io.Serial;

/**
 * Indicates framework configuration or bootstrap logic failure.
 *
 * <p>Thrown when framework initialization, bean registration, or configuration validation fails.
 * This exception typically occurs during application startup or framework setup phase.
 *
 * <p>Common scenarios:
 *
 * <ul>
 *   <li>Invalid framework configuration parameters
 *   <li>Missing required dependencies during initialization
 *   <li>Bean registration failures
 *   <li>Classpath resource loading failures
 * </ul>
 *
 * @see DatabaseTesterException
 */
public final class ConfigurationException extends DatabaseTesterException {

  /** Serial version UID for serialization compatibility. */
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs exception with detail message.
   *
   * @param message the detail message describing the configuration failure
   */
  public ConfigurationException(final String message) {
    super(message);
  }

  /**
   * Constructs exception with detail message and cause.
   *
   * @param message the detail message describing the configuration failure
   * @param cause the underlying exception that caused this failure
   */
  public ConfigurationException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs exception with cause.
   *
   * @param cause the underlying exception that caused this configuration failure
   */
  public ConfigurationException(final Throwable cause) {
    super(cause);
  }
}
