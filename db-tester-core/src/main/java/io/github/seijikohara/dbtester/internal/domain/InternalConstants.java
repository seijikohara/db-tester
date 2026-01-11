package io.github.seijikohara.dbtester.internal.domain;

/**
 * Internal constants used across the framework.
 *
 * <p>This class provides shared constant values that are used by multiple internal classes. It is
 * not part of the public API and should not be used by external code.
 *
 * <p>This class cannot be instantiated.
 */
public final class InternalConstants {

  /** Prefix for Base64-encoded binary data in CSV/TSV files. */
  public static final String BASE64_PREFIX = "[BASE64]";

  /**
   * Private constructor to prevent instantiation.
   *
   * @throws UnsupportedOperationException always
   */
  private InternalConstants() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
