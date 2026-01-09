package io.github.seijikohara.dbtester.api.config;

import io.github.seijikohara.dbtester.api.domain.ComparisonStrategy;
import java.util.Locale;

/**
 * Associates a column name with its comparison strategy for expectation verification.
 *
 * <p>This record is used to configure global column strategies in {@link ConventionSettings} and to
 * represent annotation-based column strategy configurations. Column name matching is
 * case-insensitive.
 *
 * <p>Example usage with programmatic configuration:
 *
 * <pre>{@code
 * var settings = ConventionSettings.standard()
 *     .withGlobalColumnStrategies(Map.of(
 *         "CREATED_AT", ColumnStrategyMapping.ignore("CREATED_AT"),
 *         "EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL")
 *     ));
 * }</pre>
 *
 * <p>The column name is normalized to uppercase in the constructor, so mappings created with
 * different cases of the same column name will have equal column names after construction.
 *
 * @param columnName the column name (stored in uppercase for case-insensitive matching)
 * @param strategy the comparison strategy to use for this column
 * @see ConventionSettings#globalColumnStrategies()
 * @see io.github.seijikohara.dbtester.api.annotation.ColumnStrategy
 */
public record ColumnStrategyMapping(String columnName, ComparisonStrategy strategy) {

  /**
   * Creates a ColumnStrategyMapping with the column name normalized to uppercase.
   *
   * @param columnName the column name
   * @param strategy the comparison strategy
   */
  public ColumnStrategyMapping {
    columnName = columnName.toUpperCase(Locale.ROOT);
  }

  /**
   * Creates a ColumnStrategyMapping with the specified column name and strategy.
   *
   * @param columnName the column name (case-insensitive)
   * @param strategy the comparison strategy
   * @return a new ColumnStrategyMapping instance
   */
  public static ColumnStrategyMapping of(
      final String columnName, final ComparisonStrategy strategy) {
    return new ColumnStrategyMapping(columnName, strategy);
  }

  /**
   * Creates a ColumnStrategyMapping for strict (exact match) comparison.
   *
   * <p>This is a convenience method equivalent to {@code of(columnName,
   * ComparisonStrategy.STRICT)}.
   *
   * @param columnName the column name for strict comparison
   * @return a new ColumnStrategyMapping with STRICT strategy
   */
  public static ColumnStrategyMapping strict(final String columnName) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.STRICT);
  }

  /**
   * Creates a ColumnStrategyMapping for ignoring a column.
   *
   * <p>This is a convenience method equivalent to {@code of(columnName,
   * ComparisonStrategy.IGNORE)}.
   *
   * @param columnName the column name to ignore
   * @return a new ColumnStrategyMapping with IGNORE strategy
   */
  public static ColumnStrategyMapping ignore(final String columnName) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.IGNORE);
  }

  /**
   * Creates a ColumnStrategyMapping for case-insensitive comparison.
   *
   * <p>This is a convenience method equivalent to {@code of(columnName,
   * ComparisonStrategy.CASE_INSENSITIVE)}.
   *
   * @param columnName the column name for case-insensitive comparison
   * @return a new ColumnStrategyMapping with CASE_INSENSITIVE strategy
   */
  public static ColumnStrategyMapping caseInsensitive(final String columnName) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.CASE_INSENSITIVE);
  }

  /**
   * Creates a ColumnStrategyMapping for numeric comparison.
   *
   * @param columnName the column name for numeric comparison
   * @return a new ColumnStrategyMapping with NUMERIC strategy
   */
  public static ColumnStrategyMapping numeric(final String columnName) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.NUMERIC);
  }

  /**
   * Creates a ColumnStrategyMapping for flexible timestamp comparison.
   *
   * @param columnName the column name for timestamp comparison
   * @return a new ColumnStrategyMapping with TIMESTAMP_FLEXIBLE strategy
   */
  public static ColumnStrategyMapping timestampFlexible(final String columnName) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.TIMESTAMP_FLEXIBLE);
  }

  /**
   * Creates a ColumnStrategyMapping for not-null verification.
   *
   * @param columnName the column name for not-null verification
   * @return a new ColumnStrategyMapping with NOT_NULL strategy
   */
  public static ColumnStrategyMapping notNull(final String columnName) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.NOT_NULL);
  }

  /**
   * Creates a ColumnStrategyMapping for regex pattern matching.
   *
   * @param columnName the column name for regex matching
   * @param pattern the regex pattern
   * @return a new ColumnStrategyMapping with REGEX strategy
   */
  public static ColumnStrategyMapping regex(final String columnName, final String pattern) {
    return new ColumnStrategyMapping(columnName, ComparisonStrategy.regex(pattern));
  }
}
