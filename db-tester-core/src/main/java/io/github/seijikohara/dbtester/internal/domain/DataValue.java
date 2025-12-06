package io.github.seijikohara.dbtester.internal.domain;

import org.jspecify.annotations.Nullable;

/**
 * Wrapper for a cell value that may legitimately be {@code null}.
 *
 * <p>This record provides type-safe handling of database NULL values. Use the {@link #NULL}
 * constant for representing NULL values instead of creating new instances with {@code null}.
 *
 * @param value underlying cell value; may be {@code null}
 */
public record DataValue(@Nullable Object value) {

  /** Singleton instance representing a NULL database value. */
  public static final DataValue NULL = new DataValue(null);

  /**
   * Checks if this DataValue represents a NULL database value.
   *
   * @return {@code true} if the wrapped value is null, {@code false} otherwise
   */
  public boolean isNull() {
    return value == null;
  }
}
