package io.github.seijikohara.dbtester.api.exception;

import java.io.Serial;

/**
 * Signals that a dataset could not be materialised from its backing files.
 *
 * <p>This exception is thrown when the framework fails to load or parse dataset files (CSV, JSON,
 * or other supported formats) due to I/O errors, format issues, or invalid content.
 *
 * <h2>Common Causes</h2>
 *
 * <ul>
 *   <li>File not found or not accessible
 *   <li>Invalid file format or corrupted content
 *   <li>Parsing errors in dataset structure
 *   <li>Unsupported dataset format
 * </ul>
 */
public final class DataSetLoadException extends DatabaseTesterException {

  /** Serial version UID for serialization compatibility. */
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructs a new exception with the specified detail message.
   *
   * @param message the detail message
   */
  public DataSetLoadException(final String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and cause.
   *
   * @param message the detail message
   * @param cause the underlying exception
   */
  public DataSetLoadException(final String message, final Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause.
   *
   * @param cause the underlying exception
   */
  public DataSetLoadException(final Throwable cause) {
    super(cause);
  }
}
