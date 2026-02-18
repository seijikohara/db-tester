package io.github.seijikohara.dbtester.spring.support;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;

/**
 * Converts column strategy property values to {@link ColumnStrategyMapping} instances.
 *
 * <p>This utility class provides conversion logic shared by all Spring Boot starter modules. It
 * transforms the property-bound column strategy configuration into the API types used by {@link
 * io.github.seijikohara.dbtester.api.config.VerificationSettings}.
 *
 * @see ColumnStrategyMapping
 * @see ComparisonStrategy
 */
public final class ColumnStrategyConverter {

  /** Prevents instantiation. */
  private ColumnStrategyConverter() {}

  /**
   * Converts a {@link ComparisonStrategy.Type} and optional pattern to a {@link
   * ComparisonStrategy}.
   *
   * <p>For the {@link ComparisonStrategy.Type#REGEX} type, the pattern parameter is required.
   *
   * @param type the comparison strategy type
   * @param pattern the regex pattern (required for REGEX type, ignored otherwise)
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if REGEX type is specified without a pattern
   */
  public static ComparisonStrategy toComparisonStrategy(
      final ComparisonStrategy.Type type, final @Nullable String pattern) {
    return switch (type) {
      case STRICT -> ComparisonStrategy.STRICT;
      case IGNORE -> ComparisonStrategy.IGNORE;
      case NUMERIC -> ComparisonStrategy.NUMERIC;
      case CASE_INSENSITIVE -> ComparisonStrategy.CASE_INSENSITIVE;
      case TIMESTAMP_FLEXIBLE -> ComparisonStrategy.TIMESTAMP_FLEXIBLE;
      case DATE_FLEXIBLE -> ComparisonStrategy.DATE_FLEXIBLE;
      case JSON_EQUIVALENT -> ComparisonStrategy.JSON_EQUIVALENT;
      case NOT_NULL -> ComparisonStrategy.NOT_NULL;
      case REGEX -> {
        if (pattern == null || pattern.isBlank()) {
          throw new IllegalArgumentException("pattern is required for REGEX strategy");
        }
        yield ComparisonStrategy.regex(pattern);
      }
    };
  }

  /**
   * Creates a {@link ColumnStrategyMapping} from the given column name, strategy type, and optional
   * pattern.
   *
   * @param columnName the column name (case-insensitive)
   * @param type the comparison strategy type
   * @param pattern the regex pattern (required for REGEX type, ignored otherwise)
   * @return a new ColumnStrategyMapping instance
   * @throws IllegalArgumentException if columnName is blank, or REGEX type is specified without a
   *     pattern
   */
  public static ColumnStrategyMapping toColumnStrategyMapping(
      final String columnName, final ComparisonStrategy.Type type, final @Nullable String pattern) {
    final var strategy = toComparisonStrategy(type, pattern);
    return ColumnStrategyMapping.of(columnName, strategy);
  }

  /**
   * Creates a map entry suitable for {@link
   * io.github.seijikohara.dbtester.api.config.VerificationSettings.Builder#globalColumnStrategies(Map)}.
   *
   * <p>The key is the column name in uppercase for case-insensitive matching.
   *
   * @param columnName the column name
   * @param mapping the column strategy mapping
   * @return a map entry with uppercase column name key
   */
  public static Map.Entry<String, ColumnStrategyMapping> toMapEntry(
      final String columnName, final ColumnStrategyMapping mapping) {
    return Map.entry(columnName.toUpperCase(Locale.ROOT), mapping);
  }
}
