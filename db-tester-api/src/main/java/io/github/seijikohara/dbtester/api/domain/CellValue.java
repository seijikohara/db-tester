package io.github.seijikohara.dbtester.api.domain;

import org.jspecify.annotations.Nullable;

/**
 * Wrapper for a cell value that may legitimately be {@code null}.
 *
 * <p>This record provides type-safe handling of database NULL values. Use the {@link #NULL}
 * constant for representing NULL values instead of creating new instances with {@code null}.
 *
 * @param value underlying cell value; may be {@code null}
 */
public record CellValue(@Nullable Object value) {

  /** Singleton instance representing a NULL database value. */
  public static final CellValue NULL = new CellValue(null);

  /**
   * Checks if this CellValue represents a NULL database value.
   *
   * @return {@code true} if the wrapped value is null, {@code false} otherwise
   */
  public boolean isNull() {
    return value == null;
  }
}
