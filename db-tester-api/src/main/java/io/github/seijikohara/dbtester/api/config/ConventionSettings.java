package io.github.seijikohara.dbtester.api.config;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Defines the naming conventions used to locate datasets and filter scenarios.
 *
 * <p>This class is immutable and thread-safe. Use the {@link #builder()} method to create instances
 * with custom settings, or {@link #standard()} to obtain an instance with default values.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * // Using defaults
 * var settings = ConventionSettings.standard();
 *
 * // Customizing with builder
 * var settings = ConventionSettings.builder()
 *     .expectationSuffix("/expected")
 *     .dataFormat(DataFormat.TSV)
 *     .rowOrdering(RowOrdering.UNORDERED)
 *     .build();
 *
 * // Modifying existing settings
 * var modified = settings.withRowOrdering(RowOrdering.UNORDERED);
 * }</pre>
 */
public final class ConventionSettings {

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

  /**
   * Default file name for specifying table loading order in dataset directories.
   *
   * <p>This file contains one table name per line, specifying the order in which tables should be
   * loaded during database operations.
   */
  public static final String DEFAULT_LOAD_ORDER_FILE_NAME = "load-order.txt";

  /** The base directory for dataset resolution. */
  private final @Nullable String baseDirectory;

  /** The suffix appended to base path for expectation datasets. */
  private final String expectationSuffix;

  /** The column name used to identify scenario rows. */
  private final String scenarioMarker;

  /** The data format to use when loading dataset files. */
  private final DataFormat dataFormat;

  /** The strategy for merging tables. */
  private final TableMergeStrategy tableMergeStrategy;

  /** The file name used to specify table loading order. */
  private final String loadOrderFileName;

  /** The column names to exclude from all expectation verifications globally. */
  private final Set<String> globalExcludeColumns;

  /** The column comparison strategies applied globally. */
  private final Map<String, ColumnStrategyMapping> globalColumnStrategies;

  /** The default row ordering strategy for expectation verification. */
  private final RowOrdering rowOrdering;

  /** The maximum time to wait for database queries. */
  private final @Nullable Duration queryTimeout;

  /** The number of retry attempts for expectation verification. */
  private final int retryCount;

  /** The delay between retry attempts. */
  private final Duration retryDelay;

  /** The transaction behavior for database operations. */
  private final TransactionMode transactionMode;

  /**
   * Creates a new instance from the builder.
   *
   * @param builder the builder containing configuration values
   */
  private ConventionSettings(final Builder builder) {
    this.baseDirectory = builder.baseDirectory;
    this.expectationSuffix = builder.expectationSuffix;
    this.scenarioMarker = builder.scenarioMarker;
    this.dataFormat = builder.dataFormat;
    this.tableMergeStrategy = builder.tableMergeStrategy;
    this.loadOrderFileName = builder.loadOrderFileName;
    this.globalExcludeColumns = Set.copyOf(builder.globalExcludeColumns);
    this.globalColumnStrategies = Map.copyOf(builder.globalColumnStrategies);
    this.rowOrdering = builder.rowOrdering;
    this.queryTimeout = builder.queryTimeout;
    this.retryCount = builder.retryCount;
    this.retryDelay = builder.retryDelay;
    this.transactionMode = builder.transactionMode;
  }

  /**
   * Creates a new builder for constructing ConventionSettings instances.
   *
   * @return a new builder with default values
   */
  public static Builder builder() {
    return new Builder();
  }

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
    return builder().build();
  }

  /**
   * Returns the base directory for dataset resolution.
   *
   * @return the base directory path, or null for convention-based resolution
   */
  public @Nullable String baseDirectory() {
    return baseDirectory;
  }

  /**
   * Returns the suffix appended to base path for expectation datasets.
   *
   * @return the expectation suffix
   */
  public String expectationSuffix() {
    return expectationSuffix;
  }

  /**
   * Returns the column name used to identify scenario rows.
   *
   * @return the scenario marker
   */
  public String scenarioMarker() {
    return scenarioMarker;
  }

  /**
   * Returns the data format to use when loading dataset files.
   *
   * @return the data format
   */
  public DataFormat dataFormat() {
    return dataFormat;
  }

  /**
   * Returns the strategy for merging tables when multiple DataSets contain the same table.
   *
   * @return the table merge strategy
   */
  public TableMergeStrategy tableMergeStrategy() {
    return tableMergeStrategy;
  }

  /**
   * Returns the file name used to specify table loading order.
   *
   * @return the load order file name
   */
  public String loadOrderFileName() {
    return loadOrderFileName;
  }

  /**
   * Returns the column names to exclude from all expectation verifications globally.
   *
   * @return an unmodifiable set of column names
   */
  public Set<String> globalExcludeColumns() {
    return globalExcludeColumns;
  }

  /**
   * Returns the column comparison strategies applied globally.
   *
   * @return an unmodifiable map of column strategies
   */
  public Map<String, ColumnStrategyMapping> globalColumnStrategies() {
    return globalColumnStrategies;
  }

  /**
   * Returns the default row ordering strategy for expectation verification.
   *
   * @return the row ordering strategy
   */
  public RowOrdering rowOrdering() {
    return rowOrdering;
  }

  /**
   * Returns the maximum time to wait for database queries.
   *
   * @return the query timeout duration, or null for no timeout
   */
  public @Nullable Duration queryTimeout() {
    return queryTimeout;
  }

  /**
   * Returns the number of retry attempts for expectation verification.
   *
   * @return the retry count (0 means no retry)
   */
  public int retryCount() {
    return retryCount;
  }

  /**
   * Returns the delay between retry attempts.
   *
   * @return the retry delay duration
   */
  public Duration retryDelay() {
    return retryDelay;
  }

  /**
   * Returns the transaction behavior for database operations.
   *
   * @return the transaction mode
   */
  public TransactionMode transactionMode() {
    return transactionMode;
  }

  /**
   * Creates a new ConventionSettings with the specified base directory.
   *
   * @param baseDirectory the base directory path, or null for convention-based resolution
   * @return a new ConventionSettings with the specified base directory
   */
  public ConventionSettings withBaseDirectory(final @Nullable String baseDirectory) {
    return toBuilder().baseDirectory(baseDirectory).build();
  }

  /**
   * Creates a new ConventionSettings with the specified expectation suffix.
   *
   * @param expectationSuffix the suffix appended to base path for expectation datasets
   * @return a new ConventionSettings with the specified expectation suffix
   */
  public ConventionSettings withExpectationSuffix(final String expectationSuffix) {
    return toBuilder().expectationSuffix(expectationSuffix).build();
  }

  /**
   * Creates a new ConventionSettings with the specified scenario marker.
   *
   * @param scenarioMarker the column name used to identify scenario rows
   * @return a new ConventionSettings with the specified scenario marker
   */
  public ConventionSettings withScenarioMarker(final String scenarioMarker) {
    return toBuilder().scenarioMarker(scenarioMarker).build();
  }

  /**
   * Creates a new ConventionSettings with the specified data format.
   *
   * @param dataFormat the data format to use
   * @return a new ConventionSettings with the specified data format
   */
  public ConventionSettings withDataFormat(final DataFormat dataFormat) {
    return toBuilder().dataFormat(dataFormat).build();
  }

  /**
   * Creates a new ConventionSettings with the specified table merge strategy.
   *
   * @param tableMergeStrategy the table merge strategy to use
   * @return a new ConventionSettings with the specified merge strategy
   */
  public ConventionSettings withTableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
    return toBuilder().tableMergeStrategy(tableMergeStrategy).build();
  }

  /**
   * Creates a new ConventionSettings with the specified load order file name.
   *
   * @param loadOrderFileName the load order file name to use
   * @return a new ConventionSettings with the specified load order file name
   */
  public ConventionSettings withLoadOrderFileName(final String loadOrderFileName) {
    return toBuilder().loadOrderFileName(loadOrderFileName).build();
  }

  /**
   * Creates a new ConventionSettings with the specified global exclude columns.
   *
   * @param globalExcludeColumns the column names to exclude globally
   * @return a new ConventionSettings with the specified global exclude columns
   */
  public ConventionSettings withGlobalExcludeColumns(final Set<String> globalExcludeColumns) {
    return toBuilder().globalExcludeColumns(globalExcludeColumns).build();
  }

  /**
   * Creates a new ConventionSettings with the specified global column strategies.
   *
   * @param globalColumnStrategies the column strategies to apply globally
   * @return a new ConventionSettings with the specified global column strategies
   */
  public ConventionSettings withGlobalColumnStrategies(
      final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
    return toBuilder().globalColumnStrategies(globalColumnStrategies).build();
  }

  /**
   * Creates a new ConventionSettings with the specified row ordering strategy.
   *
   * @param rowOrdering the row ordering strategy to use
   * @return a new ConventionSettings with the specified row ordering
   */
  public ConventionSettings withRowOrdering(final RowOrdering rowOrdering) {
    return toBuilder().rowOrdering(rowOrdering).build();
  }

  /**
   * Creates a new ConventionSettings with the specified query timeout.
   *
   * @param queryTimeout the query timeout duration, or null for no timeout
   * @return a new ConventionSettings with the specified query timeout
   */
  public ConventionSettings withQueryTimeout(final @Nullable Duration queryTimeout) {
    return toBuilder().queryTimeout(queryTimeout).build();
  }

  /**
   * Creates a new ConventionSettings with the specified retry count.
   *
   * @param retryCount the number of retry attempts (0 for no retry)
   * @return a new ConventionSettings with the specified retry count
   * @throws IllegalArgumentException if retryCount is negative
   */
  public ConventionSettings withRetryCount(final int retryCount) {
    return toBuilder().retryCount(retryCount).build();
  }

  /**
   * Creates a new ConventionSettings with the specified retry delay.
   *
   * @param retryDelay the delay between retry attempts
   * @return a new ConventionSettings with the specified retry delay
   */
  public ConventionSettings withRetryDelay(final Duration retryDelay) {
    return toBuilder().retryDelay(retryDelay).build();
  }

  /**
   * Creates a new ConventionSettings with the specified transaction mode.
   *
   * @param transactionMode the transaction mode to use
   * @return a new ConventionSettings with the specified transaction mode
   */
  public ConventionSettings withTransactionMode(final TransactionMode transactionMode) {
    return toBuilder().transactionMode(transactionMode).build();
  }

  /**
   * Creates a new builder initialized with the values from this instance.
   *
   * @return a new builder with values copied from this instance
   */
  public Builder toBuilder() {
    return new Builder()
        .baseDirectory(this.baseDirectory)
        .expectationSuffix(this.expectationSuffix)
        .scenarioMarker(this.scenarioMarker)
        .dataFormat(this.dataFormat)
        .tableMergeStrategy(this.tableMergeStrategy)
        .loadOrderFileName(this.loadOrderFileName)
        .globalExcludeColumns(this.globalExcludeColumns)
        .globalColumnStrategies(this.globalColumnStrategies)
        .rowOrdering(this.rowOrdering)
        .queryTimeout(this.queryTimeout)
        .retryCount(this.retryCount)
        .retryDelay(this.retryDelay)
        .transactionMode(this.transactionMode);
  }

  @Override
  public boolean equals(final @Nullable Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ConventionSettings other)) {
      return false;
    }
    return Objects.equals(baseDirectory, other.baseDirectory)
        && Objects.equals(expectationSuffix, other.expectationSuffix)
        && Objects.equals(scenarioMarker, other.scenarioMarker)
        && dataFormat == other.dataFormat
        && tableMergeStrategy == other.tableMergeStrategy
        && Objects.equals(loadOrderFileName, other.loadOrderFileName)
        && Objects.equals(globalExcludeColumns, other.globalExcludeColumns)
        && Objects.equals(globalColumnStrategies, other.globalColumnStrategies)
        && rowOrdering == other.rowOrdering
        && Objects.equals(queryTimeout, other.queryTimeout)
        && retryCount == other.retryCount
        && Objects.equals(retryDelay, other.retryDelay)
        && transactionMode == other.transactionMode;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        baseDirectory,
        expectationSuffix,
        scenarioMarker,
        dataFormat,
        tableMergeStrategy,
        loadOrderFileName,
        globalExcludeColumns,
        globalColumnStrategies,
        rowOrdering,
        queryTimeout,
        retryCount,
        retryDelay,
        transactionMode);
  }

  @Override
  public String toString() {
    return "ConventionSettings["
        + "baseDirectory="
        + baseDirectory
        + ", expectationSuffix="
        + expectationSuffix
        + ", scenarioMarker="
        + scenarioMarker
        + ", dataFormat="
        + dataFormat
        + ", tableMergeStrategy="
        + tableMergeStrategy
        + ", loadOrderFileName="
        + loadOrderFileName
        + ", globalExcludeColumns="
        + globalExcludeColumns
        + ", globalColumnStrategies="
        + globalColumnStrategies
        + ", rowOrdering="
        + rowOrdering
        + ", queryTimeout="
        + queryTimeout
        + ", retryCount="
        + retryCount
        + ", retryDelay="
        + retryDelay
        + ", transactionMode="
        + transactionMode
        + ']';
  }

  /** Builder for constructing {@link ConventionSettings} instances. */
  public static final class Builder {

    /** The base directory for dataset resolution. */
    private @Nullable String baseDirectory = null;

    /** The suffix appended to base path for expectation datasets. */
    private String expectationSuffix = DEFAULT_EXPECTATION_SUFFIX;

    /** The column name used to identify scenario rows. */
    private String scenarioMarker = DEFAULT_SCENARIO_MARKER;

    /** The data format to use when loading dataset files. */
    private DataFormat dataFormat = DataFormat.CSV;

    /** The strategy for merging tables. */
    private TableMergeStrategy tableMergeStrategy = TableMergeStrategy.UNION_ALL;

    /** The file name used to specify table loading order. */
    private String loadOrderFileName = DEFAULT_LOAD_ORDER_FILE_NAME;

    /** The column names to exclude from all expectation verifications globally. */
    private Set<String> globalExcludeColumns = Set.of();

    /** The column comparison strategies applied globally. */
    private Map<String, ColumnStrategyMapping> globalColumnStrategies = Map.of();

    /** The default row ordering strategy for expectation verification. */
    private RowOrdering rowOrdering = RowOrdering.ORDERED;

    /** The maximum time to wait for database queries. */
    private @Nullable Duration queryTimeout = null;

    /** The number of retry attempts for expectation verification. */
    private int retryCount = 0;

    /** The delay between retry attempts. */
    private Duration retryDelay = Duration.ofMillis(100);

    /** The transaction behavior for database operations. */
    private TransactionMode transactionMode = TransactionMode.SINGLE_TRANSACTION;

    /** Creates a new builder with default values. */
    public Builder() {}

    /**
     * Sets the base directory for dataset resolution.
     *
     * @param baseDirectory the base directory path, or null for convention-based resolution
     * @return this builder
     */
    public Builder baseDirectory(final @Nullable String baseDirectory) {
      this.baseDirectory = baseDirectory;
      return this;
    }

    /**
     * Sets the suffix appended to base path for expectation datasets.
     *
     * @param expectationSuffix the expectation suffix
     * @return this builder
     */
    public Builder expectationSuffix(final String expectationSuffix) {
      this.expectationSuffix = Objects.requireNonNull(expectationSuffix, "expectationSuffix");
      return this;
    }

    /**
     * Sets the column name used to identify scenario rows.
     *
     * @param scenarioMarker the scenario marker
     * @return this builder
     */
    public Builder scenarioMarker(final String scenarioMarker) {
      this.scenarioMarker = Objects.requireNonNull(scenarioMarker, "scenarioMarker");
      return this;
    }

    /**
     * Sets the data format to use when loading dataset files.
     *
     * @param dataFormat the data format
     * @return this builder
     */
    public Builder dataFormat(final DataFormat dataFormat) {
      this.dataFormat = Objects.requireNonNull(dataFormat, "dataFormat");
      return this;
    }

    /**
     * Sets the strategy for merging tables when multiple DataSets contain the same table.
     *
     * @param tableMergeStrategy the table merge strategy
     * @return this builder
     */
    public Builder tableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
      this.tableMergeStrategy = Objects.requireNonNull(tableMergeStrategy, "tableMergeStrategy");
      return this;
    }

    /**
     * Sets the file name used to specify table loading order.
     *
     * @param loadOrderFileName the load order file name
     * @return this builder
     */
    public Builder loadOrderFileName(final String loadOrderFileName) {
      this.loadOrderFileName = Objects.requireNonNull(loadOrderFileName, "loadOrderFileName");
      return this;
    }

    /**
     * Sets the column names to exclude from all expectation verifications globally.
     *
     * @param globalExcludeColumns the column names to exclude
     * @return this builder
     */
    public Builder globalExcludeColumns(final Set<String> globalExcludeColumns) {
      this.globalExcludeColumns =
          Objects.requireNonNull(globalExcludeColumns, "globalExcludeColumns");
      return this;
    }

    /**
     * Sets the column comparison strategies applied globally.
     *
     * @param globalColumnStrategies the column strategies
     * @return this builder
     */
    public Builder globalColumnStrategies(
        final Map<String, ColumnStrategyMapping> globalColumnStrategies) {
      this.globalColumnStrategies =
          Objects.requireNonNull(globalColumnStrategies, "globalColumnStrategies");
      return this;
    }

    /**
     * Sets the default row ordering strategy for expectation verification.
     *
     * @param rowOrdering the row ordering strategy
     * @return this builder
     */
    public Builder rowOrdering(final RowOrdering rowOrdering) {
      this.rowOrdering = Objects.requireNonNull(rowOrdering, "rowOrdering");
      return this;
    }

    /**
     * Sets the maximum time to wait for database queries.
     *
     * @param queryTimeout the query timeout duration, or null for no timeout
     * @return this builder
     */
    public Builder queryTimeout(final @Nullable Duration queryTimeout) {
      this.queryTimeout = queryTimeout;
      return this;
    }

    /**
     * Sets the number of retry attempts for expectation verification.
     *
     * @param retryCount the retry count (0 means no retry)
     * @return this builder
     * @throws IllegalArgumentException if retryCount is negative
     */
    public Builder retryCount(final int retryCount) {
      if (retryCount < 0) {
        throw new IllegalArgumentException("retryCount must not be negative");
      }
      this.retryCount = retryCount;
      return this;
    }

    /**
     * Sets the delay between retry attempts.
     *
     * @param retryDelay the retry delay duration
     * @return this builder
     */
    public Builder retryDelay(final Duration retryDelay) {
      this.retryDelay = Objects.requireNonNull(retryDelay, "retryDelay");
      return this;
    }

    /**
     * Sets the transaction behavior for database operations.
     *
     * @param transactionMode the transaction mode
     * @return this builder
     */
    public Builder transactionMode(final TransactionMode transactionMode) {
      this.transactionMode = Objects.requireNonNull(transactionMode, "transactionMode");
      return this;
    }

    /**
     * Builds a new {@link ConventionSettings} instance with the configured values.
     *
     * @return a new ConventionSettings instance
     */
    public ConventionSettings build() {
      return new ConventionSettings(this);
    }
  }
}
