package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.config.TransactionMode;
import io.github.seijikohara.dbtester.api.operation.Operation;
import java.time.Duration;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration properties for DB Tester Spring Boot integration.
 *
 * <p>These properties control how DataSource beans from the Spring application context are
 * registered with the {@link io.github.seijikohara.dbtester.api.config.DataSourceRegistry}, as well
 * as default conventions and operations for database testing.
 *
 * <p>Properties are prefixed with {@code db-tester}.
 *
 * <h2>Available Properties</h2>
 *
 * <ul>
 *   <li>{@code db-tester.enabled} - Enable/disable DB Tester (default: true)
 *   <li>{@code db-tester.auto-register-data-sources} - Auto-register DataSources (default: true)
 *   <li>{@code db-tester.convention.*} - Convention settings for dataset resolution
 *   <li>{@code db-tester.verification.*} - Verification settings for expectation behavior
 *   <li>{@code db-tester.execution.*} - Execution settings for database operation behavior
 *   <li>{@code db-tester.operation.*} - Default database operations
 * </ul>
 */
@ConfigurationProperties(prefix = "db-tester")
public class DbTesterProperties {

  /** Creates a new instance with default property values. */
  public DbTesterProperties() {
    // Default constructor for Spring Boot configuration binding
  }

  /** Whether DB Tester is enabled. Defaults to true. */
  private boolean enabled = true;

  /** Whether to automatically register DataSource beans. Defaults to true. */
  private boolean autoRegisterDataSources = true;

  /** Convention settings for dataset resolution. */
  @NestedConfigurationProperty private ConventionProperties convention = new ConventionProperties();

  /** Verification settings for expectation behavior. */
  @NestedConfigurationProperty
  private VerificationProperties verification = new VerificationProperties();

  /** Execution settings for database operation behavior. */
  @NestedConfigurationProperty private ExecutionProperties execution = new ExecutionProperties();

  /** Default operation settings for preparation and expectation phases. */
  @NestedConfigurationProperty private OperationProperties operation = new OperationProperties();

  /**
   * Returns whether DB Tester is enabled.
   *
   * @return true if enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Sets whether DB Tester is enabled.
   *
   * @param enabled true to enable
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
  }

  /**
   * Returns whether to automatically register DataSource beans with the DataSourceRegistry.
   *
   * @return true if auto-registration is enabled
   */
  public boolean isAutoRegisterDataSources() {
    return autoRegisterDataSources;
  }

  /**
   * Sets whether to automatically register DataSource beans with the DataSourceRegistry.
   *
   * @param autoRegisterDataSources true to enable auto-registration
   */
  public void setAutoRegisterDataSources(final boolean autoRegisterDataSources) {
    this.autoRegisterDataSources = autoRegisterDataSources;
  }

  /**
   * Returns the convention properties.
   *
   * @return the convention properties
   */
  public ConventionProperties getConvention() {
    return convention;
  }

  /**
   * Sets the convention properties.
   *
   * @param convention the convention properties
   */
  public void setConvention(final ConventionProperties convention) {
    this.convention = convention;
  }

  /**
   * Returns the verification properties.
   *
   * @return the verification properties
   */
  public VerificationProperties getVerification() {
    return verification;
  }

  /**
   * Sets the verification properties.
   *
   * @param verification the verification properties
   */
  public void setVerification(final VerificationProperties verification) {
    this.verification = verification;
  }

  /**
   * Returns the execution properties.
   *
   * @return the execution properties
   */
  public ExecutionProperties getExecution() {
    return execution;
  }

  /**
   * Sets the execution properties.
   *
   * @param execution the execution properties
   */
  public void setExecution(final ExecutionProperties execution) {
    this.execution = execution;
  }

  /**
   * Returns the operation properties.
   *
   * @return the operation properties
   */
  public OperationProperties getOperation() {
    return operation;
  }

  /**
   * Sets the operation properties.
   *
   * @param operation the operation properties
   */
  public void setOperation(final OperationProperties operation) {
    this.operation = operation;
  }

  /**
   * Convention properties for dataset resolution.
   *
   * <p>These properties define how dataset files are located and processed.
   *
   * <h2>Available Properties</h2>
   *
   * <ul>
   *   <li>{@code db-tester.convention.base-directory} - Base directory for datasets (default: null,
   *       uses classpath)
   *   <li>{@code db-tester.convention.expectation-suffix} - Suffix for expectation directories
   *       (default: {@value ConventionSettings#DEFAULT_EXPECTATION_SUFFIX})
   *   <li>{@code db-tester.convention.scenario-marker} - Column name for scenario filtering
   *       (default: {@value ConventionSettings#DEFAULT_SCENARIO_MARKER})
   *   <li>{@code db-tester.convention.data-format} - Dataset file format (default: CSV)
   *   <li>{@code db-tester.convention.table-merge-strategy} - Strategy for merging tables (default:
   *       UNION_ALL)
   *   <li>{@code db-tester.convention.load-order-file-name} - File name for table loading order
   *       (default: {@value ConventionSettings#DEFAULT_LOAD_ORDER_FILE_NAME})
   * </ul>
   */
  public static class ConventionProperties {

    /** Creates a new instance with default values. */
    public ConventionProperties() {
      // Default constructor for Spring Boot configuration binding
    }

    /**
     * Base directory for dataset resolution. Null means resolve from classpath relative to test
     * class.
     */
    private @Nullable String baseDirectory;

    /**
     * Suffix appended to preparation path for expectation datasets. Defaults to {@value
     * ConventionSettings#DEFAULT_EXPECTATION_SUFFIX}.
     */
    private String expectationSuffix = ConventionSettings.DEFAULT_EXPECTATION_SUFFIX;

    /**
     * Column name that identifies scenario markers in dataset files. Defaults to {@value
     * ConventionSettings#DEFAULT_SCENARIO_MARKER}.
     */
    private String scenarioMarker = ConventionSettings.DEFAULT_SCENARIO_MARKER;

    /** File format for dataset files. Defaults to AUTO. */
    private DataFormat dataFormat = DataFormat.AUTO;

    /** Strategy for merging tables from multiple datasets. Defaults to UNION_ALL. */
    private TableMergeStrategy tableMergeStrategy = TableMergeStrategy.UNION_ALL;

    /**
     * File name for specifying table loading order in dataset directories. Defaults to {@value
     * ConventionSettings#DEFAULT_LOAD_ORDER_FILE_NAME}.
     */
    private String loadOrderFileName = ConventionSettings.DEFAULT_LOAD_ORDER_FILE_NAME;

    /**
     * Returns the base directory for dataset resolution.
     *
     * @return the base directory, or null for classpath resolution
     */
    public @Nullable String getBaseDirectory() {
      return baseDirectory;
    }

    /**
     * Sets the base directory for dataset resolution.
     *
     * @param baseDirectory the base directory, or null for classpath resolution
     */
    public void setBaseDirectory(final @Nullable String baseDirectory) {
      this.baseDirectory = baseDirectory;
    }

    /**
     * Returns the suffix for expectation directories.
     *
     * @return the expectation suffix
     */
    public String getExpectationSuffix() {
      return expectationSuffix;
    }

    /**
     * Sets the suffix for expectation directories.
     *
     * @param expectationSuffix the expectation suffix
     */
    public void setExpectationSuffix(final String expectationSuffix) {
      this.expectationSuffix = expectationSuffix;
    }

    /**
     * Returns the scenario marker column name.
     *
     * @return the scenario marker
     */
    public String getScenarioMarker() {
      return scenarioMarker;
    }

    /**
     * Sets the scenario marker column name.
     *
     * @param scenarioMarker the scenario marker
     */
    public void setScenarioMarker(final String scenarioMarker) {
      this.scenarioMarker = scenarioMarker;
    }

    /**
     * Returns the data format for dataset files.
     *
     * @return the data format
     */
    public DataFormat getDataFormat() {
      return dataFormat;
    }

    /**
     * Sets the data format for dataset files.
     *
     * @param dataFormat the data format
     */
    public void setDataFormat(final DataFormat dataFormat) {
      this.dataFormat = dataFormat;
    }

    /**
     * Returns the table merge strategy.
     *
     * @return the table merge strategy
     */
    public TableMergeStrategy getTableMergeStrategy() {
      return tableMergeStrategy;
    }

    /**
     * Sets the table merge strategy.
     *
     * @param tableMergeStrategy the table merge strategy
     */
    public void setTableMergeStrategy(final TableMergeStrategy tableMergeStrategy) {
      this.tableMergeStrategy = tableMergeStrategy;
    }

    /**
     * Returns the load order file name.
     *
     * @return the load order file name
     */
    public String getLoadOrderFileName() {
      return loadOrderFileName;
    }

    /**
     * Sets the load order file name.
     *
     * @param loadOrderFileName the load order file name
     */
    public void setLoadOrderFileName(final String loadOrderFileName) {
      this.loadOrderFileName = loadOrderFileName;
    }
  }

  /**
   * Verification properties for expectation behavior.
   *
   * <p>These properties control how the framework verifies expected database state after test
   * execution.
   *
   * <h2>Available Properties</h2>
   *
   * <ul>
   *   <li>{@code db-tester.verification.global-exclude-columns} - Column names to exclude globally
   *       (default: empty)
   *   <li>{@code db-tester.verification.row-ordering} - Row ordering strategy (default: ORDERED)
   *   <li>{@code db-tester.verification.retry-count} - Retry attempts (default: 0)
   *   <li>{@code db-tester.verification.retry-delay} - Delay between retries (default: 100ms)
   * </ul>
   */
  public static class VerificationProperties {

    /** Creates a new instance with default values. */
    public VerificationProperties() {
      // Default constructor for Spring Boot configuration binding
    }

    /** Column names to exclude globally from all expectation verifications. */
    private Set<String> globalExcludeColumns = Set.of();

    /** Row ordering strategy for expectation verification. Defaults to ORDERED. */
    private RowOrdering rowOrdering = RowOrdering.ORDERED;

    /** Number of retry attempts for expectation verification. Defaults to 0. */
    private int retryCount = 0;

    /** Delay between retry attempts. Defaults to 100ms. */
    private Duration retryDelay = Duration.ofMillis(100);

    /**
     * Returns the global exclude columns.
     *
     * @return the column names to exclude globally
     */
    public Set<String> getGlobalExcludeColumns() {
      return globalExcludeColumns;
    }

    /**
     * Sets the global exclude columns.
     *
     * @param globalExcludeColumns the column names to exclude globally
     */
    public void setGlobalExcludeColumns(final Set<String> globalExcludeColumns) {
      this.globalExcludeColumns = globalExcludeColumns;
    }

    /**
     * Returns the row ordering strategy.
     *
     * @return the row ordering
     */
    public RowOrdering getRowOrdering() {
      return rowOrdering;
    }

    /**
     * Sets the row ordering strategy.
     *
     * @param rowOrdering the row ordering
     */
    public void setRowOrdering(final RowOrdering rowOrdering) {
      this.rowOrdering = rowOrdering;
    }

    /**
     * Returns the retry count.
     *
     * @return the retry count
     */
    public int getRetryCount() {
      return retryCount;
    }

    /**
     * Sets the retry count.
     *
     * @param retryCount the retry count
     */
    public void setRetryCount(final int retryCount) {
      this.retryCount = retryCount;
    }

    /**
     * Returns the retry delay.
     *
     * @return the retry delay
     */
    public Duration getRetryDelay() {
      return retryDelay;
    }

    /**
     * Sets the retry delay.
     *
     * @param retryDelay the retry delay
     */
    public void setRetryDelay(final Duration retryDelay) {
      this.retryDelay = retryDelay;
    }
  }

  /**
   * Execution properties for database operation behavior.
   *
   * <p>These properties control how the framework executes database operations during test
   * preparation.
   *
   * <h2>Available Properties</h2>
   *
   * <ul>
   *   <li>{@code db-tester.execution.query-timeout} - Maximum query wait time (default: none)
   *   <li>{@code db-tester.execution.transaction-mode} - Transaction behavior (default:
   *       SINGLE_TRANSACTION)
   * </ul>
   */
  public static class ExecutionProperties {

    /** Creates a new instance with default values. */
    public ExecutionProperties() {
      // Default constructor for Spring Boot configuration binding
    }

    /** Maximum time to wait for database queries. Null means no timeout. */
    private @Nullable Duration queryTimeout = null;

    /** Transaction behavior for database operations. Defaults to SINGLE_TRANSACTION. */
    private TransactionMode transactionMode = TransactionMode.SINGLE_TRANSACTION;

    /**
     * Returns the query timeout.
     *
     * @return the query timeout, or null for no timeout
     */
    public @Nullable Duration getQueryTimeout() {
      return queryTimeout;
    }

    /**
     * Sets the query timeout.
     *
     * @param queryTimeout the query timeout, or null for no timeout
     */
    public void setQueryTimeout(final @Nullable Duration queryTimeout) {
      this.queryTimeout = queryTimeout;
    }

    /**
     * Returns the transaction mode.
     *
     * @return the transaction mode
     */
    public TransactionMode getTransactionMode() {
      return transactionMode;
    }

    /**
     * Sets the transaction mode.
     *
     * @param transactionMode the transaction mode
     */
    public void setTransactionMode(final TransactionMode transactionMode) {
      this.transactionMode = transactionMode;
    }
  }

  /**
   * Operation properties for database test phases.
   *
   * <p>These properties define the default operations executed during test preparation and
   * expectation verification.
   *
   * <h2>Available Properties</h2>
   *
   * <ul>
   *   <li>{@code db-tester.operation.preparation} - Default preparation operation (default:
   *       CLEAN_INSERT)
   *   <li>{@code db-tester.operation.expectation} - Default expectation operation (default: NONE)
   * </ul>
   */
  public static class OperationProperties {

    /** Creates a new instance with default values. */
    public OperationProperties() {
      // Default constructor for Spring Boot configuration binding
    }

    /** Default operation for test preparation phase. Defaults to CLEAN_INSERT. */
    private Operation preparation = Operation.CLEAN_INSERT;

    /** Default operation for expectation verification phase. Defaults to NONE. */
    private Operation expectation = Operation.NONE;

    /**
     * Returns the default preparation operation.
     *
     * @return the preparation operation
     */
    public Operation getPreparation() {
      return preparation;
    }

    /**
     * Sets the default preparation operation.
     *
     * @param preparation the preparation operation
     */
    public void setPreparation(final Operation preparation) {
      this.preparation = preparation;
    }

    /**
     * Returns the default expectation operation.
     *
     * @return the expectation operation
     */
    public Operation getExpectation() {
      return expectation;
    }

    /**
     * Sets the default expectation operation.
     *
     * @param expectation the expectation operation
     */
    public void setExpectation(final Operation expectation) {
      this.expectation = expectation;
    }
  }
}
