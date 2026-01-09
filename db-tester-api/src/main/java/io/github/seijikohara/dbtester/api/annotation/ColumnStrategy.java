package io.github.seijikohara.dbtester.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures the comparison strategy for a specific column during expectation verification.
 *
 * <p>This annotation allows fine-grained control over how individual columns are compared when
 * validating database state against expected values. It is used within {@link DataSetSource} to
 * specify column-level comparison behavior.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * @ExpectedDataSet(dataSets = @DataSetSource(
 *     columnStrategies = {
 *         @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
 *         @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
 *         @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}")
 *     }
 * ))
 * void testUserCreation() { }
 * }</pre>
 *
 * <p>Column name matching is case-insensitive. For example, specifying {@code "EMAIL"} will match
 * columns named {@code "email"}, {@code "EMAIL"}, or {@code "Email"}.
 *
 * <p>This annotation only applies to expectation verification (when used within {@link
 * ExpectedDataSet#dataSets()}). It has no effect when used within {@link DataSet#dataSets()} for
 * preparation.
 *
 * @see Strategy
 * @see DataSetSource#columnStrategies()
 * @see io.github.seijikohara.dbtester.api.domain.ComparisonStrategy
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface ColumnStrategy {

  /**
   * The column name to apply this strategy to.
   *
   * <p>Column name matching is case-insensitive. The name should match the column as defined in the
   * database or CSV/TSV file.
   *
   * @return the column name
   */
  String name();

  /**
   * The comparison strategy to use for this column.
   *
   * <p>Defaults to {@link Strategy#STRICT} which requires exact value matching.
   *
   * @return the comparison strategy
   * @see Strategy
   */
  Strategy strategy() default Strategy.STRICT;

  /**
   * The regular expression pattern for {@link Strategy#REGEX} comparison.
   *
   * <p>This attribute is only used when {@link #strategy()} is set to {@link Strategy#REGEX}. The
   * actual database value must match this pattern for the comparison to succeed.
   *
   * <p>Example patterns:
   *
   * <ul>
   *   <li>{@code "[a-f0-9-]{36}"} - UUID format
   *   <li>{@code "\\d{4}-\\d{2}-\\d{2}"} - Date format (YYYY-MM-DD)
   *   <li>{@code ".*@example\\.com"} - Email domain validation
   * </ul>
   *
   * @return the regex pattern, or empty string if not using REGEX strategy
   */
  String pattern() default "";
}
