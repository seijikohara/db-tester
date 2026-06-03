package io.github.seijikohara.dbtester.spring.support;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import io.github.seijikohara.dbtester.api.domain.Strategy;
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
   * Converts a {@link Strategy} and optional pattern to a {@link ComparisonStrategy}.
   *
   * <p>For {@link Strategy#REGEX}, the pattern parameter is required.
   *
   * @param strategy the comparison strategy type
   * @param pattern the regex pattern (required for REGEX, ignored otherwise)
   * @return the corresponding ComparisonStrategy instance
   * @throws IllegalArgumentException if REGEX is specified without a pattern
   */
  public static ComparisonStrategy toComparisonStrategy(
      final Strategy strategy, final @Nullable String pattern) {
    return ComparisonStrategy.of(strategy, pattern);
  }

  /**
   * Creates a {@link ColumnStrategyMapping} from the given column name, strategy, and optional
   * pattern.
   *
   * @param columnName the column name (case-insensitive)
   * @param strategy the comparison strategy type
   * @param pattern the regex pattern (required for REGEX, ignored otherwise)
   * @return a new ColumnStrategyMapping instance
   * @throws IllegalArgumentException if columnName is blank, or REGEX is specified without a
   *     pattern
   */
  public static ColumnStrategyMapping toColumnStrategyMapping(
      final String columnName, final Strategy strategy, final @Nullable String pattern) {
    return ColumnStrategyMapping.of(columnName, ComparisonStrategy.of(strategy, pattern));
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
