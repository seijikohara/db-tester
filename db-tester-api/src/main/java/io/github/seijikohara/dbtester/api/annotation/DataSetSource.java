package io.github.seijikohara.dbtester.api.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a dataset source to be consumed by the database testing extension.
 *
 * <p>The annotation describes three independent concerns:
 *
 * <ol>
 *   <li><strong>Where to load the data from.</strong> The {@link #resourceLocation()} attribute can
 *       point to a classpath directory or any readable file-system path. When the attribute is left
 *       empty, the loader falls back to the project conventions for preparation and expectation
 *       phases.
 *   <li><strong>Which data source to execute against.</strong> {@link #dataSourceName()} selects a
 *       named entry from {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry}; an
 *       empty value delegates to the default registration.
 *   <li><strong>How to filter scenario-specific rows.</strong> {@link #scenarioNames()} narrows the
 *       dataset to the named scenarios when the underlying format exposes a scenario marker column.
 *       If omitted, the current test method name becomes the only scenario.
 * </ol>
 *
 * <p>{@code @DataSetSource} is not applied directly to test classes or methods. Instead it augments
 * the {@link DataSet#sources()} and {@link ExpectedDataSet#sources()} containers that describe the
 * per-phase datasets.
 */
@Target({})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSetSource {

  /**
   * Declares the logical name of the {@link javax.sql.DataSource} that should execute the dataset.
   *
   * <p>The name must match an entry registered via {@link
   * io.github.seijikohara.dbtester.api.config.DataSourceRegistry}. Supplying an empty string (the
   * default) routes the dataset to the registry's default entry.
   *
   * @return the configured data source identifier, or an empty string for the default registration
   */
  String dataSourceName() default "";

  /**
   * Provides the root location from which dataset files are loaded.
   *
   * <p>The value accepts the following forms:
   *
   * <ul>
   *   <li>{@code classpath:...} &mdash; resolved relative to the current class loader
   *   <li>Absolute or relative file-system paths understood by {@link java.nio.file.Path}
   * </ul>
   *
   * <p>When left empty the loader derives a directory using the convention settings in {@link
   * io.github.seijikohara.dbtester.api.config.ConventionSettings}, combining the test class package
   * and appropriate phase suffixes for post-test verification.
   *
   * @return the directory that contains the dataset files, or an empty string for convention-based
   *     discovery
   */
  String resourceLocation() default "";

  /**
   * Lists the scenario identifiers that should be retained when loading the dataset.
   *
   * <p>If the dataset format exposes a scenario marker column (for example, the default CSV format
   * uses {@code [Scenario]}), only rows whose marker matches one of the supplied names are
   * included. Providing an empty array delegates the decision to the framework, which uses the test
   * method name as the single scenario.
   *
   * @return scenario names to keep, or an empty array to fall back to the test method name
   */
  String[] scenarioNames() default {};

  /**
   * Lists the column names to exclude from assertion verification.
   *
   * <p>Columns listed here are ignored during database state comparison. This is useful for
   * excluding auto-generated columns (timestamps, version numbers, auto-increment IDs) that cannot
   * be predicted in test data.
   *
   * <p>Column name matching is case-insensitive. For example, specifying {@code "CREATED_AT"} will
   * exclude columns named {@code "created_at"}, {@code "CREATED_AT"}, or {@code "Created_At"}.
   *
   * <p>This attribute only applies to expectation verification (when used within {@link
   * ExpectedDataSet#sources()}). It has no effect when used within {@link DataSet#sources()} for
   * preparation.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * @ExpectedDataSet(sources = @DataSetSource(
   *     excludeColumns = {"CREATED_AT", "UPDATED_AT", "VERSION"}
   * ))
   * void testUserCreation() { }
   * }</pre>
   *
   * @return column names to exclude from verification, or an empty array for no exclusions
   * @see io.github.seijikohara.dbtester.api.assertion.DatabaseAssertion#assertEqualsIgnoreColumns
   */
  String[] excludeColumns() default {};

  /**
   * Lists the column-specific comparison strategies for expectation verification.
   *
   * <p>Each {@link ColumnStrategy} defines how a specific column should be compared during database
   * state verification. This allows fine-grained control over comparison behavior for individual
   * columns.
   *
   * <p>Column strategies override the default strict comparison. For columns not listed here, the
   * default {@link Strategy#STRICT} comparison is used unless a global column strategy is
   * configured in {@link io.github.seijikohara.dbtester.api.config.ConventionSettings}.
   *
   * <p>This attribute only applies to expectation verification (when used within {@link
   * ExpectedDataSet#sources()}). It has no effect when used within {@link DataSet#sources()} for
   * preparation.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * @ExpectedDataSet(sources = @DataSetSource(
   *     columnStrategies = {
   *         @ColumnStrategy(name = "EMAIL", strategy = Strategy.CASE_INSENSITIVE),
   *         @ColumnStrategy(name = "CREATED_AT", strategy = Strategy.IGNORE),
   *         @ColumnStrategy(name = "ID", strategy = Strategy.REGEX, pattern = "[a-f0-9-]{36}")
   *     }
   * ))
   * void testUserCreation() { }
   * }</pre>
   *
   * @return column strategies for verification, or an empty array for default (strict) comparison
   * @see ColumnStrategy
   * @see Strategy
   * @see io.github.seijikohara.dbtester.api.domain.ComparisonStrategy
   */
  ColumnStrategy[] columnStrategies() default {};
}
