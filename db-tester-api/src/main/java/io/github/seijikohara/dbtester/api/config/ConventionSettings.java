package io.github.seijikohara.dbtester.api.config;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Defines the naming conventions used to locate datasets and filter scenarios.
 *
 * @param baseDirectory optional absolute or relative directory that anchors all datasets; {@code
 *     null} instructs the loader to resolve locations from the classpath
 * @param expectationSuffix directory appended to the preparation path when resolving expectation
 *     datasets
 * @param scenarioMarker column name that denotes the scenario marker used by scenario-aware formats
 * @param dataFormat the file format to use when loading dataset files (CSV or TSV)
 * @param tableMergeStrategy the strategy for merging tables when multiple DataSets contain the same
 *     table
 * @param loadOrderFileName the file name used to specify table loading order in dataset directories
 * @param globalExcludeColumns column names to exclude from all expectation verifications globally
 * @param globalColumnStrategies column comparison strategies applied to all expectation
 *     verifications globally, keyed by uppercase column name
 * @param rowOrdering the default row ordering strategy for expectation verification
 * @param queryTimeout the maximum time to wait for database queries; {@code null} for no timeout
 * @param retryCount the number of retry attempts for expectation verification (0 for no retry)
 * @param retryDelay the delay between retry attempts
 * @param transactionMode the transaction behavior for database operations
 */
public record ConventionSettings(
    @Nullable String baseDirectory,
    String expectationSuffix,
    String scenarioMarker,
    DataFormat dataFormat,
    TableMergeStrategy tableMergeStrategy,
    String loadOrderFileName,
    Set<String> globalExcludeColumns,
    Map<String, ColumnStrategyMapping> globalColumnStrategies,
    RowOrdering rowOrdering,
    @Nullable Duration queryTimeout,
    int retryCount,
    Duration retryDelay,
    TransactionMode transactionMode) {

  /**
   * Default base directory for dataset resolution.
   *
   * <p>A {@code null} value instructs the loader to resolve dataset locations relative to the test
   * class package on the classpath.
   */
  private static final @Nullable String DEFAULT_BASE_DIRECTORY = null;

  /**
   * Default suffix appended to the preparation directory when resolving expectation datasets.
   *
   * <p>This suffix is typically a subdirectory name that separates expected outcome data from
   * preparation data.
   */
  public static final String DEFAULT_EXPECTATION_SUFFIX = "/expected";

  /**
   * Default column name that identifies scenario markers in scenario-aware dataset formats.
   *
   * <p>Rows containing this column are filtered based on scenario names specified in test
   * annotations or derived from test method names.
   */
  public static final String DEFAULT_SCENARIO_MARKER = "[Scenario]";

  /** Default file format for dataset files. */
  private static final DataFormat DEFAULT_DATA_FORMAT = DataFormat.CSV;

  /** Default strategy for merging tables from multiple datasets. */
  private static final TableMergeStrategy DEFAULT_TABLE_MERGE_STRATEGY =
      TableMergeStrategy.UNION_ALL;

  /**
   * Default file name for specifying table loading order in dataset directories.
   *
   * <p>This file contains one table name per line, specifying the order in which tables should be
   * loaded during database operations.
   */
  public static final String DEFAULT_LOAD_ORDER_FILE_NAME = "load-order.txt";

  /** Default global exclude columns (empty set). */
  private static final Set<String> DEFAULT_GLOBAL_EXCLUDE_COLUMNS = Set.of();

  /** Default global column strategies (empty map). */
  private static final Map<String, ColumnStrategyMapping> DEFAULT_GLOBAL_COLUMN_STRATEGIES =
      Map.of();

  /** Default row ordering strategy for expectation verification. */
  private static final RowOrdering DEFAULT_ROW_ORDERING = RowOrdering.ORDERED;

  /** Default query timeout (null means no timeout). */
  private static final @Nullable Duration DEFAULT_QUERY_TIMEOUT = null;

  /** Default retry count (0 means no retry). */
  private static final int DEFAULT_RETRY_COUNT = 0;

  /** Default retry delay (100 milliseconds). */
  private static final Duration DEFAULT_RETRY_DELAY = Duration.ofMillis(100);

  /** Default transaction mode. */
  private static final TransactionMode DEFAULT_TRANSACTION_MODE =
      TransactionMode.SINGLE_TRANSACTION;

  /**
   * Creates a convention instance populated with the framework defaults.
   *
   * @return conventions using classpath-relative discovery, {@value #DEFAULT_EXPECTATION_SUFFIX}
   *     suffix, {@value #DEFAULT_SCENARIO_MARKER} marker, CSV format, UNION_ALL merge strategy,
   *     {@value #DEFAULT_LOAD_ORDER_FILE_NAME} load order file, no global exclude columns, no
   *     global column strategies, ORDERED row ordering, no query timeout, no retry, and
   *     SINGLE_TRANSACTION transaction mode
   */
  public static ConventionSettings standard() {
    return new ConventionSettings(
        DEFAULT_BASE_DIRECTORY,
        DEFAULT_EXPECTATION_SUFFIX,
        DEFAULT_SCENARIO_MARKER,
        DEFAULT_DATA_FORMAT,
        DEFAULT_TABLE_MERGE_STRATEGY,
        DEFAULT_LOAD_ORDER_FILE_NAME,
        DEFAULT_GLOBAL_EXCLUDE_COLUMNS,
        DEFAULT_GLOBAL_COLUMN_STRATEGIES,
        DEFAULT_ROW_ORDERING,
        DEFAULT_QUERY_TIMEOUT,
        DEFAULT_RETRY_COUNT,
        DEFAULT_RETRY_DELAY,
        DEFAULT_TRANSACTION_MODE);
  }

  /**
   * Creates a new ConventionSettings with the specified base directory.
   *
   * <p>The base directory can be an absolute path or a path relative to the classpath. A {@code
   * null} value instructs the loader to resolve dataset locations relative to the test class
   * package on the classpath.
   *
   * @param baseDirectory the base directory path, or null for convention-based resolution
   * @return a new ConventionSettings with the specified base directory
   */
  public ConventionSettings withBaseDirectory(final @Nullable String baseDirectory) {
    return new ConventionSettings(
        baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified expectation suffix.
   *
   * <p>The expectation suffix is appended to the preparation directory when resolving expectation
   * datasets. This suffix is typically a subdirectory name that separates expected outcome data
   * from preparation data.
   *
   * @param expectationSuffix the suffix appended to base path for expectation datasets
   * @return a new ConventionSettings with the specified expectation suffix
   */
  public ConventionSettings withExpectationSuffix(final String expectationSuffix) {
    return new ConventionSettings(
        this.baseDirectory,
        expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified scenario marker.
   *
   * <p>The scenario marker is the column name that identifies scenario markers in scenario-aware
   * dataset formats. Rows containing this column are filtered based on scenario names specified in
   * test annotations or derived from test method names.
   *
   * @param scenarioMarker the column name used to identify scenario rows
   * @return a new ConventionSettings with the specified scenario marker
   */
  public ConventionSettings withScenarioMarker(final String scenarioMarker) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified data format.
   *
   * @param dataFormat the data format to use
   * @return a new ConventionSettings with the specified data format
   */
  public ConventionSettings withDataFormat(final DataFormat dataFormat) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified table merge strategy.
   *
   * @param tableMergeStrategy the table merge strategy to use
   * @return a new ConventionSettings with the specified merge strategy
   */
  public ConventionSettings withTableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified load order file name.
   *
   * @param loadOrderFileName the load order file name to use
   * @return a new ConventionSettings with the specified load order file name
   */
  public ConventionSettings withLoadOrderFileName(final String loadOrderFileName) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified global exclude columns.
   *
   * <p>Columns listed here are excluded from all expectation verifications. This is useful for
   * excluding auto-generated columns (timestamps, version numbers) across all tests without
   * repeating the exclusion in each annotation.
   *
   * <p>Column name matching is case-insensitive.
   *
   * @param globalExcludeColumns the column names to exclude globally
   * @return a new ConventionSettings with the specified global exclude columns
   */
  public ConventionSettings withGlobalExcludeColumns(final Set<String> globalExcludeColumns) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified global column strategies.
   *
   * <p>Column strategies defined here are applied to all expectation verifications unless
   * overridden by annotation-level {@link
   * io.github.seijikohara.dbtester.api.annotation.ColumnStrategy} configurations. This is useful
   * for applying consistent comparison strategies across all tests without repeating the
   * configuration in each annotation.
   *
   * <p>The map keys are uppercase column names for case-insensitive matching.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * var settings = ConventionSettings.standard()
   *     .withGlobalColumnStrategies(Map.of(
   *         "CREATED_AT", ColumnStrategyMapping.ignore("CREATED_AT"),
   *         "UPDATED_AT", ColumnStrategyMapping.ignore("UPDATED_AT"),
   *         "EMAIL", ColumnStrategyMapping.caseInsensitive("EMAIL")
   *     ));
   * }</pre>
   *
   * @param globalColumnStrategies the column strategies to apply globally
   * @return a new ConventionSettings with the specified global column strategies
   * @see ColumnStrategyMapping
   */
  public ConventionSettings withGlobalColumnStrategies(
      final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified row ordering strategy.
   *
   * <p>The row ordering strategy determines how rows are compared during expectation verification.
   * Use {@link RowOrdering#ORDERED} for positional comparison (default) or {@link
   * RowOrdering#UNORDERED} for set-based comparison.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * var settings = ConventionSettings.standard()
   *     .withRowOrdering(RowOrdering.UNORDERED);
   * }</pre>
   *
   * @param rowOrdering the row ordering strategy to use
   * @return a new ConventionSettings with the specified row ordering
   * @see RowOrdering
   */
  public ConventionSettings withRowOrdering(final RowOrdering rowOrdering) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified query timeout.
   *
   * <p>The query timeout specifies the maximum time to wait for database queries. A {@code null}
   * value means no timeout (queries can run indefinitely).
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * var settings = ConventionSettings.standard()
   *     .withQueryTimeout(Duration.ofSeconds(30));
   * }</pre>
   *
   * @param queryTimeout the query timeout duration, or null for no timeout
   * @return a new ConventionSettings with the specified query timeout
   */
  public ConventionSettings withQueryTimeout(final @Nullable Duration queryTimeout) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        queryTimeout,
        this.retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified retry count.
   *
   * <p>The retry count specifies the number of additional attempts for expectation verification
   * after the first failure. A value of 0 means no retry (single attempt only).
   *
   * <p>Retry is useful for eventual consistency scenarios where the database state may not be
   * immediately consistent after the test action.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * var settings = ConventionSettings.standard()
   *     .withRetryCount(3)
   *     .withRetryDelay(Duration.ofMillis(500));
   * }</pre>
   *
   * @param retryCount the number of retry attempts (0 for no retry)
   * @return a new ConventionSettings with the specified retry count
   * @throws IllegalArgumentException if retryCount is negative
   */
  public ConventionSettings withRetryCount(final int retryCount) {
    if (retryCount < 0) {
      throw new IllegalArgumentException("retryCount must not be negative");
    }
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        retryCount,
        this.retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified retry delay.
   *
   * <p>The retry delay specifies the time to wait between retry attempts for expectation
   * verification. This delay allows transient inconsistencies to resolve before the next attempt.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * var settings = ConventionSettings.standard()
   *     .withRetryCount(3)
   *     .withRetryDelay(Duration.ofMillis(500));
   * }</pre>
   *
   * @param retryDelay the delay between retry attempts
   * @return a new ConventionSettings with the specified retry delay
   */
  public ConventionSettings withRetryDelay(final Duration retryDelay) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        retryDelay,
        this.transactionMode);
  }

  /**
   * Creates a new ConventionSettings with the specified transaction mode.
   *
   * <p>The transaction mode controls how transactions are managed during database operations. See
   * {@link TransactionMode} for available options.
   *
   * <p>Example usage:
   *
   * <pre>{@code
   * var settings = ConventionSettings.standard()
   *     .withTransactionMode(TransactionMode.AUTO_COMMIT);
   * }</pre>
   *
   * @param transactionMode the transaction mode to use
   * @return a new ConventionSettings with the specified transaction mode
   * @see TransactionMode
   */
  public ConventionSettings withTransactionMode(final TransactionMode transactionMode) {
    return new ConventionSettings(
        this.baseDirectory,
        this.expectationSuffix,
        this.scenarioMarker,
        this.dataFormat,
        this.tableMergeStrategy,
        this.loadOrderFileName,
        this.globalExcludeColumns,
        this.globalColumnStrategies,
        this.rowOrdering,
        this.queryTimeout,
        this.retryCount,
        this.retryDelay,
        transactionMode);
  }
}
