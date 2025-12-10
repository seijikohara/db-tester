package io.github.seijikohara.dbtester.junit.spring.boot.autoconfigure;

import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.DataFormat;
import io.github.seijikohara.dbtester.api.config.TableMergeStrategy;
import io.github.seijikohara.dbtester.api.operation.Operation;
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

    /** File format for dataset files. Defaults to CSV. */
    private DataFormat dataFormat = DataFormat.CSV;

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
